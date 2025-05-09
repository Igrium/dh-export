package com.igrium.dist_export;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.igrium.dist_export.command.DistExportCommand;
import com.igrium.dist_export.mesh_utils.LodQuadHolder;
import com.igrium.dist_export.mesh_utils.MergedObjWriter;
import com.igrium.dist_export.mesh_utils.ObjMeshBuilder;
import com.igrium.dist_export.util.SimpleDhSectionPos;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiLevelLoadEvent;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.level.*;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.render.LodQuadTree;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import de.javagl.obj.Obj;
import de.javagl.obj.Objs;
import loaderCommon.fabric.com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DHExport implements ModInitializer {
		public static final String MOD_ID = "dist_export";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static DHExport instance;

	public static DHExport getInstance() {
		return instance;
	}

	private final Cache<ClientWorld, Map<SimpleDhSectionPos, LodQuadBuilder>> lodCache = CacheBuilder.newBuilder().weakKeys().build();

	@Nullable
	@Deprecated
	private IDhClientLevel currentClientLevel;

	@Deprecated
	public @Nullable IDhClientLevel getCurrentClientLevel() {
		return currentClientLevel;
	}

	@Deprecated
	public void setCurrentClientLevel(@Nullable IDhClientLevel currentClientLevel) {
		LOGGER.info("Setting current client level: {}", currentClientLevel);
		this.currentClientLevel = currentClientLevel;
	}


	@Nullable
	private LodQuadTree currentQuadTree;

	@Nullable
	public LodQuadTree getCurrentQuadTree() {
		return currentQuadTree;
	}

	public void setCurrentQuadTree(@Nullable LodQuadTree currentQuadTree) {
		this.currentQuadTree = currentQuadTree;
	}

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

	public CompletableFuture<Integer> exportDHWorld(Path path, @Nullable Consumer<String> feedbackConsumer) {
		try {
            LodQuadTree quadTree;
//            if (currentClientLevel instanceof DhClientLevel cLevel) {
//                quadTree = cLevel.clientside.ClientRenderStateRef.get().quadtree;
//            } else if (currentClientLevel instanceof DhClientServerLevel cLevel) {
//				quadTree = cLevel.clientside.ClientRenderStateRef.get().quadtree;
//			} else {
//				throw new IllegalStateException("No DH world loaded.");
//			}
			if (currentQuadTree == null) {
				throw new IllegalStateException("No DH world loaded.");
			}
            return exportDHWorld(path, currentQuadTree, feedbackConsumer);
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletableFuture<Integer> exportDHWorld(Path path, LodQuadTree quadTree, @Nullable Consumer<String> feedbackConsumer) {

		AtomicInteger currentIndex = new AtomicInteger();
		ConcurrentHashMap<String, Obj> objs = new ConcurrentHashMap<>();
		List<CompletableFuture<?>> futures = new ArrayList<>(quadTree.leafNodeCount());

		if (feedbackConsumer != null)
			feedbackConsumer.accept("Compiling chunks");

		quadTree.leafNodeIterator().forEachRemaining(node -> {
			futures.add(CompletableFuture.runAsync(() -> {
				int index = currentIndex.getAndIncrement();
				String name = "chunk." + index;

                LOGGER.info("Building mesh for chunk {}", index);
				Obj obj = renderSection(node.value);
				if (obj != null)
					objs.put(name, obj);
			}, Util.getMainWorkerExecutor()));
		});

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenApplyAsync(v -> {
			LOGGER.info("Saving obj");
            if (feedbackConsumer != null)
                feedbackConsumer.accept("Saving obj");

			try(BufferedWriter writer = Files.newBufferedWriter(path)) {
				MergedObjWriter.writeObjects(objs, writer);
				return objs.size();
			} catch (IOException e) {
				throw new CompletionException(e);
			}
        });

	}

	@Nullable
	public Obj renderSection(LodRenderSection section) {
		int x = DhSectionPos.getX(section.pos);
		int z = DhSectionPos.getZ(section.pos);

		LodQuadBuilder mesh = LodQuadHolder.get(section).dist_export$getLodQuads();
		if (mesh == null) {
            LOGGER.warn("No mesh found for section {}", section);
			return null;
		}

		ObjMeshBuilder builder = new ObjMeshBuilder(Objs.create());
		builder.addQuads(mesh, new Vec3i(x, 0, z));

		return builder.getObj();
	}

	@Deprecated
	public CompletableFuture<?> exportDHWorldOld(Path path, ClientWorld world) {
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

		for (var entry : cache.entrySet()) {
			futures.add(CompletableFuture.runAsync(() -> {
				Obj obj = Objs.create();
				ObjMeshBuilder builder = new ObjMeshBuilder(obj);
				Vec3i pos = new Vec3i(entry.getKey().getMinCornerBlockX(), 0, entry.getKey().getMinCornerBlockZ());

				builder.addQuads(entry.getValue(), pos);
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
};