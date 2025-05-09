package com.igrium.dist_export;

import com.igrium.dist_export.command.DistExportCommand;
import com.igrium.dist_export.mesh_utils.LodQuadHolder;
import com.igrium.dist_export.mesh_utils.MergedObjWriter;
import com.igrium.dist_export.mesh_utils.ObjMeshBuilder;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.render.LodQuadTree;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import de.javagl.obj.Obj;
import de.javagl.obj.Objs;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DistExport implements ModInitializer {
	public static final String MOD_ID = "dist_export";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Getter
    private static DistExport instance;

    @Nullable
	private LodQuadTree currentQuadTree;

	public @Nullable LodQuadTree getCurrentQuadTree() {
		return currentQuadTree;
	}

	public void setCurrentQuadTree(@Nullable LodQuadTree currentQuadTree) {
		this.currentQuadTree = currentQuadTree;
	}

	@NotNull
	@Getter
	@Setter
	private Set<LodRenderSection> loadedRenderSections = Collections.emptySet();

	@Override
	public void onInitialize() {
		instance = this;

		ClientCommandRegistrationCallback.EVENT.register(DistExportCommand::register);
	}

	public CompletableFuture<Integer> exportDHWorld(Path path, @Nullable Consumer<String> feedbackConsumer) {
		try {
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

		for (var section : loadedRenderSections) {
			futures.add(CompletableFuture.runAsync(() -> {
				int index = currentIndex.getAndIncrement();
				String name = "chunk." + index;
				LOGGER.debug("Building mesh for chunk {}", index);
				Obj obj = renderSection(section);
				if (obj != null)
					objs.put(name, obj);
			}, Util.getMainWorkerExecutor()));
		}

//		quadTree.leafNodeIterator().forEachRemaining(node -> {
//			futures.add(CompletableFuture.runAsync(() -> {
//				int index = currentIndex.getAndIncrement();
//				String name = "chunk." + index;
//
//				LOGGER.debug("Building mesh for chunk {}", index);
//				Obj obj = renderSection(node.value);
//				if (obj != null)
//					objs.put(name, obj);
//			}, Util.getMainWorkerExecutor()));
//		});

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
		int x = DhSectionPos.getMinCornerBlockX(section.pos);
		int z = DhSectionPos.getMinCornerBlockZ(section.pos);

		LodQuadBuilder mesh = LodQuadHolder.get(section).dist_export$getLodQuads();
		if (mesh == null) {
			LOGGER.warn("No mesh found for section {}", section);
			return null;
		}

		LOGGER.info("Section coordinates: x: {}, z: {}", x, z);
		ObjMeshBuilder builder = new ObjMeshBuilder(Objs.create());
		builder.addQuads(mesh, new Vec3i(x, 0, z));

		return builder.getObj();
	}
//
//	public CompletableFuture<?> exportDHWorldOld(Path path, ClientWorld world) {
//        try {
//            Files.createDirectories(path.getParent());
//        } catch (IOException e) {
//			return CompletableFuture.failedFuture(e);
//        }
//
//        var cache = lodCache.getIfPresent(world);
//		if (cache == null) {
//			LOGGER.warn("World does not have any DH data.");
//			return CompletableFuture.completedFuture(null);
//		}
//
//		Map<String, Obj> objs = new ConcurrentHashMap<>();
//		AtomicInteger currentIndex = new AtomicInteger();
//
//		List<CompletableFuture<?>> futures = new ArrayList<>(cache.size());
//
//		if (cache.isEmpty()) {
//			LOGGER.warn("No DH data found.");
//		}
//
//		for (var mesh : cache.values()) {
//			futures.add(CompletableFuture.runAsync(() -> {
//				Obj obj = Objs.create();
//				ObjMeshBuilder builder = new ObjMeshBuilder(obj);
//
//				builder.addQuads(mesh,Vec3i.ZERO);
//				int index = currentIndex.getAndIncrement();
//				objs.put("chunk." + index, obj);
//				LOGGER.info("Compiled chunk " + index);
//			}, Util.getMainWorkerExecutor()));
//		}
//
//		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRunAsync(() -> {
//			LOGGER.info("Assembling {} DH chunks", futures.size());
//			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//
//				MergedObjWriter.writeObjects(objs, writer);
//
//			} catch (IOException e) {
//				throw new CompletionException(e);
//			}
//
//		}, Util.getIoWorkerExecutor());
//	}
}