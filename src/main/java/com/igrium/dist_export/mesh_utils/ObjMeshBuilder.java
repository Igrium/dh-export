package com.igrium.dist_export.mesh_utils;

import com.igrium.dist_export.mixin.LodQuadBuilderAccessor;
import com.igrium.dist_export.util.RecursiveIterable;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.BufferQuad;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodQuadBuilder;
import com.seibel.distanthorizons.core.enums.EDhDirection;
import de.javagl.obj.Obj;

/**
 * Converts a DH LOD into an Obj mesh object.
 */
public class ObjMeshBuilder {
    private final Obj obj;

    public ObjMeshBuilder(Obj obj) {
        this.obj = obj;
    }

    public Obj getObj() {
        return obj;
    }

    public void addQuads(LodQuadBuilder quadBuilder) {

        LodQuadBuilderAccessor accessor = (LodQuadBuilderAccessor) quadBuilder;

        var opaque = accessor.getOpaqueQuads();
        var transparent = accessor.getTransparentQuads();

        for (var quad : new RecursiveIterable<>(opaque)) {
            addQuad(quad);
        }

        for (var quad : new RecursiveIterable<>(opaque)) {
            addQuad(quad);
        }
    }

    private void addQuad(BufferQuad quad) {
        int index = obj.getNumVertices();

        int[][] quadBase = LodQuadBuilder.DIRECTION_VERTEX_IBO_QUAD[quad.direction.ordinal()];

        short widthEastWest = quad.widthEastWest;
        short widthNorthSouth = quad.widthNorthSouthOrUpDown;
        byte normalIndex = (byte) quad.direction.ordinal();
        EDhDirection.Axis axis = quad.direction.getAxis();

        int[] vertexIndices = new int[quadBase.length]; // Store vertex indices for the face

        for (int i = 0; i < quadBase.length; i++) {
            short dx, dy, dz;
            float wx, wy, wz;
            int nx, ny, nz;

            switch (axis) {
                case X: // ZY
                    dx = 0;
                    dy = quadBase[i][1] == 1 ? widthNorthSouth : 0;
                    dz = quadBase[i][0] == 1 ? widthEastWest : 0;
                    nx = 0;
                    ny = quadBase[i][1] == 1 ? 1 : -1;
                    nz = quadBase[i][0] == 1 ? 1 : -1;
                    break;
                case Y: // XZ
                    dx = quadBase[i][0] == 1 ? widthEastWest : 0;
                    dy = 0;
                    dz = quadBase[i][1] == 1 ? widthNorthSouth : 0;
                    nx = quadBase[i][0] == 1 ? 1 : -1;
                    ny = 0;
                    nz = quadBase[i][1] == 1 ? 1 : -1;
                    break;
                case Z: // XY
                    dx = quadBase[i][0] == 1 ? widthEastWest : 0;
                    dy = quadBase[i][1] == 1 ? widthNorthSouth : 0;
                    dz = 0;
                    nx = quadBase[i][0] == 1 ? 1 : -1;
                    ny = quadBase[i][1] == 1 ? 1 : -1;
                    nz = 0;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Axis enum: " + axis);
            }


            // Vertex position in world space
            wx = quad.x + dx;
            wy = quad.y + dy;
            wz = quad.z + dz;

            vertexIndices[i] = obj.getNumVertices();
            obj.addVertex(wx, wy, wz);

            int color = quad.color;

            // Use custom side color logic for grass blocks
            // TODO: implement this

            // TODO: Blender's implementation of vertex colors.
        }

        obj.addFace(vertexIndices);
    }
}
