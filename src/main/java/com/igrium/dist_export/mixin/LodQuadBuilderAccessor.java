package com.igrium.dist_export.mixin;

import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.BufferQuad;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = LodQuadBuilder.class, remap = false)
public interface LodQuadBuilderAccessor {
    @Accessor
    public ArrayList<BufferQuad>[] getOpaqueQuads();

    @Accessor
    public ArrayList<BufferQuad>[] getTransparentQuads();
}
