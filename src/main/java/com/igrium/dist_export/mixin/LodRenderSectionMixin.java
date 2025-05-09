package com.igrium.dist_export.mixin;

import com.igrium.dist_export.mesh_utils.LodQuadHolder;
import com.igrium.dist_export.util.SimpleDhSectionPos;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LodRenderSection.class, remap = false)
public class LodRenderSectionMixin implements LodQuadHolder {

    @Final
    @Shadow
    public long pos;

    @Final
    @Shadow
    private IDhClientLevel level;

    /**
     * The quads that were most recently pushed to the GPU.
     */
    @Unique
    @Nullable
    private LodQuadBuilder quads;

    @Inject(method = "uploadToGpuAsync", at = @At("HEAD"))
    private void dist_export$uploadToGpuAsync(LodQuadBuilder lodQuadBuilder, CallbackInfo ci) {
        SimpleDhSectionPos sectionPos = SimpleDhSectionPos.decodeFromLong(pos);
        this.quads = lodQuadBuilder;
    }

    @Override
    @Nullable
    public LodQuadBuilder dist_export$getLodQuads() {
        return quads;
    }
}