package com.igrium.dist_export.mesh_utils;

import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.render.LodRenderSection;

/**
 * A class that can hold CPU-side LOD quads.
 */
public interface LodQuadHolder {
    LodQuadBuilder dist_export$getLodQuads();

    static LodQuadHolder get(LodRenderSection section) {
        return ((LodQuadHolder) section);
    }
}