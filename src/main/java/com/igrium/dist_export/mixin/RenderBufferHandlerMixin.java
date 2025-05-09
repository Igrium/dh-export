package com.igrium.dist_export.mixin;

import com.igrium.dist_export.DistExport;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.core.render.LodQuadTree;
import com.seibel.distanthorizons.core.render.LodRenderSection;
import com.seibel.distanthorizons.core.render.RenderBufferHandler;
import com.seibel.distanthorizons.core.util.math.Vec3f;
import com.seibel.distanthorizons.core.util.objects.quadTree.QuadNode;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mixin(value = RenderBufferHandler.class, remap = false)
public class RenderBufferHandlerMixin {

    @Final
    @Shadow
    public LodQuadTree lodQuadTree;

    @Inject(method = "<init>", at = @At("RETURN"))
    void onInit(LodQuadTree lodQuadTree, CallbackInfo ci) {
        DistExport.getInstance().setCurrentQuadTree(lodQuadTree);
    }

    @Inject(method = "buildRenderListAndUpdateSections", at = @At("RETURN"))
    void onBuildRenderList(IClientLevelWrapper clientLevelWrapper, DhApiRenderParam renderEventParam, Vec3f lookForwardVector, CallbackInfo ci) {
        Iterator<QuadNode<LodRenderSection>> nodeIterator = lodQuadTree.nodeIterator();
        Set<LodRenderSection> set = new HashSet<>();
        while (nodeIterator.hasNext()) {
            QuadNode<LodRenderSection> node = nodeIterator.next();

//            long sectionPos = node.sectionPos;
            LodRenderSection renderSection = node.value;

            if (renderSection == null)
                continue;

            set.add(renderSection);
        }

        DistExport.getInstance().setLoadedRenderSections(set);
    }
}
