package com.tom.morewires.compat.sfm.util;

import blusunrize.immersiveengineering.api.wires.*;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.util.SFMDirections;
import com.tom.morewires.MoreImmersiveWires;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public final class SFMIEAdjacency {
    private SFMIEAdjacency() {}

    /** True if this position should be allowed as a BFS vertex (cable OR wire-node). */
    public static boolean isTraversable(Level level, BlockPos pos) {
        if (CableNetwork.isCable(level, pos)) return true;
        return isWireNode(level, pos);
    }

    /** True if IE considers this position a connectable participating in our SFM wire type graph. */
    public static boolean isWireNode(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IImmersiveConnectable connectable)) return false;

        GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
        if (global == null) return false;

        for (ConnectionPoint cp : connectable.getConnectionPoints()) {
            LocalWireNetwork local = global.getNullableLocalNet(cp);
            if (local == null) continue;

            for (Connection conn : local.getConnections(cp)) {
                if (conn.type != MoreImmersiveWires.SFM_WIRE.simple().wireType) continue;
                return true;
            }
        }
        return false;
    }

    /**
     * Emits neighbors for traversal:
     * - If current is an SFM cable: 6 physical neighbors (so SFM blocks still connect normally)
     * - If current is an IE connectable: wire neighbors across SFM wire connections (connectors AND relays)
     */
    public static void forEachNetworkNeighbor(Level level, BlockPos current, Consumer<BlockPos> out) {
        // Physical adjacency only matters for actual SFM cable blocks
        if (CableNetwork.isCable(level, current)) {
            BlockPos.MutableBlockPos tmp = new BlockPos.MutableBlockPos();
            for (Direction d : SFMDirections.DIRECTIONS_WITHOUT_NULL) {
                tmp.set(current).move(d);
                // Only traverse to another cable block (prevents "air vertices")
                if (CableNetwork.isCable(level, tmp)) {
                    out.accept(tmp.immutable());
                }
            }
        }

        // Wire adjacency: any IE connectable (connector OR relay)
        BlockEntity be = level.getBlockEntity(current);
        if (!(be instanceof IImmersiveConnectable connectable)) return;

        GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
        if (global == null) return;

        for (ConnectionPoint cp : connectable.getConnectionPoints()) {
            LocalWireNetwork local = global.getNullableLocalNet(cp);
            if (local == null) continue;

            for (Connection conn : local.getConnections(cp)) {
                if (conn.type != MoreImmersiveWires.SFM_WIRE.simple().wireType) continue;

                ConnectionPoint other = conn.getEndA().equals(cp) ? conn.getEndB() : conn.getEndA();
                out.accept(other.position());
            }
        }

    }
}
