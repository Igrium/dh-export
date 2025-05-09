package com.igrium.dist_export;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.igrium.dist_export.command.DistExportCommand;
import com.igrium.dist_export.mesh_utils.MergedObjWriter;
import com.igrium.dist_export.mesh_utils.ObjMeshBuilder;
import com.igrium.dist_export.util.SimpleDhSectionPos;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.world.IDhWorld;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import de.javagl.obj.Obj;
import de.javagl.obj.Objs;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistExport implements ModInitializer {
		public static final String MOD_ID = "dist_export";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static DistExport instance;

	public static DistExport getInstance() {
		return instance;
	}

	private final Cache<ClientWorld, Map<SimpleDhSectionPos, LodQuadBuilder>> lodCache = CacheBuilder.newBuilder().weakKeys().build();

	@Override
	public void onInitialize() {
		instance = this;

		ClientCommandRegistrationCallback.EVENT.register(DistExportCommand::register);
	}

	/**
	 * Get the LOD cache for a given level. This can be used to access
	 * @param level Level to use.
	 * @return The LOD cache.
	 * @apiNote This method and the returned map is 100% thread-safe.
	 */
	public Map<SimpleDhSectionPos, LodQuadBuilder> getLodCache(ClientWorld level) {
        try {
            return lodCache.get(level, ConcurrentHashMap::new);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

	public void cacheSection(ClientWorld level, SimpleDhSectionPos section, LodQuadBuilder mesh) {
		getLodCache(level).put(section, mesh);
	}

	public void cacheSection(IClientLevelWrapper level, SimpleDhSectionPos section, LodQuadBuilder mesh) {
		getLodCache((ClientWorld) level.getWrappedMcObject()).put(section, mesh);
	}

	public CompletableFuture<?> exportDHWorld(Path path, ClientWorld world) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
			return CompletableFuture.failedFuture(e);
        }

        var cache = lodCache.getIfPresent(world);
		if (cache == null) {
			LOGGER.warn("World does not have any DH data.");
			return CompletableFuture.completedFuture(null);
		}

		Map<String, Obj> objs = new ConcurrentHashMap<>();
		AtomicInteger currentIndex = new AtomicInteger();

		List<CompletableFuture<?>> futures = new ArrayList<>(cache.size());

		if (cache.isEmpty()) {
			LOGGER.warn("No DH data found.");
		}

		for (var mesh : cache.values()) {
			futures.add(CompletableFuture.runAsync(() -> {
				Obj obj = Objs.create();
				ObjMeshBuilder builder = new ObjMeshBuilder(obj);

				builder.addQuads(mesh);
				int index = currentIndex.getAndIncrement();
				objs.put("chunk." + index, obj);
				LOGGER.info("Compiled chunk " + index);
			}, Util.getMainWorkerExecutor()));
		}

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRunAsync(() -> {
			LOGGER.info("Assembling {} DH chunks", futures.size());
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {

				MergedObjWriter.writeObjects(objs, writer);

			} catch (IOException e) {
				throw new CompletionException(e);
			}

		}, Util.getIoWorkerExecutor());
	}
}