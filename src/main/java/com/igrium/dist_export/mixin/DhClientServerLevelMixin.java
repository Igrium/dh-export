package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DHExport;
import com.seibel.distanthorizons.core.file.structure.ISaveStructure;
import com.seibel.distanthorizons.core.level.DhClientServerLevel;
import com.seibel.distanthorizons.core.multiplayer.server.ServerPlayerStateManager;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IServerLevelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DhClientServerLevel.class, remap = false)
public class DhClientServerLevelMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ISaveStructure saveStructure, IServerLevelWrapper serverLevelWrapper, ServerPlayerStateManager serverPlayerStateManager, CallbackInfo ci) {
        DHExport.getInstance().setCurrentClientLevel((DhClientServerLevel)(Object) this);
    }
}
