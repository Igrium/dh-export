package com.igrium.dist_export.util;

import com.seibel.distanthorizons.core.pos.DhSectionPos;

@Deprecated
public record SimpleDhSectionPos(int x, int z) {
    public long encodeToLong(byte detailLevel) {
        return DhSectionPos.encode(detailLevel, x, z);
    }

    public int getMinCornerBlockX() {
        return DhSectionPos.getMinCornerBlockX(encodeToLong((byte) 0));
    }

    public int getMinCornerBlockZ() {
        return DhSectionPos.getMinCornerBlockZ(encodeToLong((byte) 0));
    }

    public static SimpleDhSectionPos decodeFromLong(long pos) {
        return new SimpleDhSectionPos(DhSectionPos.getX(pos), DhSectionPos.getZ(pos));
    }
}
