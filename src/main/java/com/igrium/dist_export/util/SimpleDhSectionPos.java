package com.igrium.dist_export.util;

import com.seibel.distanthorizons.core.pos.DhSectionPos;

public record SimpleDhSectionPos(int x, int z) {
    public long encodeToLong(byte detailLevel) {
        return DhSectionPos.encode(detailLevel, x, z);
    }

    public static SimpleDhSectionPos decodeFromLong(long pos) {
        return new SimpleDhSectionPos(DhSectionPos.getX(pos), DhSectionPos.getZ(pos));
    }
}
