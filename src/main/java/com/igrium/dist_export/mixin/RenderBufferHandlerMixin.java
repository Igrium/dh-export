package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DistExport;
import com.seibel.distanthorizons.core.render.LodQuadTree;
import com.seibel.distanthorizons.core.render.RenderBufferHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderBufferHandler.class, remap = false)
public class RenderBufferHandlerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    void onInit(LodQuadTree lodQuadTree, CallbackInfo ci) {
        DistExport.getInstance().setCurrentQuadTree(lodQuadTree);
    }
}
