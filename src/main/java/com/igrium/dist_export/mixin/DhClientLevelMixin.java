package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DHExport;
import com.seibel.distanthorizons.core.file.structure.ISaveStructure;
import com.seibel.distanthorizons.core.level.DhClientLevel;
import com.seibel.distanthorizons.core.multiplayer.client.ClientNetworkState;
import com.seibel.distanthorizons.core.multiplayer.server.ServerPlayerStateManager;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IServerLevelWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(value = DhClientLevel.class, remap = false)
public class DhClientLevelMixin {
    @Inject(at = @At("RETURN"), method = "<init>(Lcom/seibel/distanthorizons/core/file/structure/ISaveStructure;Lcom/seibel/distanthorizons/core/wrapperInterfaces/world/IClientLevelWrapper;Ljava/io/File;ZLcom/seibel/distanthorizons/core/multiplayer/client/ClientNetworkState;)V")
    private void dh_export$onInit(ISaveStructure saveStructure, IClientLevelWrapper clientLevelWrapper, @Nullable File fullDataSaveDirOverride,
                             boolean enableRendering, @Nullable ClientNetworkState networkState, CallbackInfo ci) {
        DHExport.getInstance().setCurrentClientLevel((DhClientLevel)(Object) this);
    }
}
