package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DHExport;
import com.seibel.distanthorizons.core.file.fullDatafile.FullDataSourceProviderV2;
import com.seibel.distanthorizons.core.level.ClientLevelModule;
import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.render.LodQuadTree;
import com.seibel.distanthorizons.core.render.renderer.generic.GenericObjectRenderer;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevelModule.ClientRenderState.class, remap = false)
public class ClientLevelModuleMixin {

    @Final
    @Shadow
    public LodQuadTree quadtree;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(IDhClientLevel dhClientLevel, IClientLevelWrapper clientLevelWrapper, FullDataSourceProviderV2 fullDataSourceProvider, GenericObjectRenderer genericRenderer, CallbackInfo ci) {
        DHExport.getInstance().setCurrentQuadTree(quadtree);
    }
}
