package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DistExport;
import com.igrium.dist_export.util.SimpleDhSectionPos;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LodRenderSection.class, remap = false)
public class LodRenderSectionMixin {

    @Final
    @Shadow
    public long pos;

    @Final
    @Shadow
    private IDhClientLevel level;

    @Inject(method = "uploadToGpuAsync", at = @At("HEAD"))
    private void dist_export$uploadToGpuAsync(LodQuadBuilder lodQuadBuilder, CallbackInfo ci) {
        SimpleDhSectionPos sectionPos = SimpleDhSectionPos.decodeFromLong(pos);
        DistExport.getInstance().cacheSection(level.getClientLevelWrapper(), sectionPos, lodQuadBuilder);

    }
}
