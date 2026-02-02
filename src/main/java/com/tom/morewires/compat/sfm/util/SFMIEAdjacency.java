package com.tom.morewires.compat.sfm.util;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.util.SFMDirections;
import com.tom.morewires.MoreImmersiveWires;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public final class SFMIEAdjacency {
    private SFMIEAdjacency() {}

    /**
     * Emits "neighbor cable positions" by:
     *  - 6-direction physical adjacency
     *  - plus: any CableNetwork.isCable positions reachable by MoreImmersiveWires.SFM_WIRE through
     *          IE connectors/relays (relay pass-through supported).
     */
    public static void forEachCableNeighbor(Level level, BlockPos current, Consumer<BlockPos> out) {
        // 6 physical neighbors
        BlockPos.MutableBlockPos tmp = new BlockPos.MutableBlockPos();
        for (Direction d : SFMDirections.DIRECTIONS_WITHOUT_NULL) {
            tmp.set(current).move(d);
            out.accept(tmp.immutable());
        }

        // Wire neighbors (relay-aware)
        GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
        if (global == null) return;

        // Start only if THIS position is an IE connectable (connectors are; plain SFM cables usually aren't)
        BlockEntity startBE = level.getBlockEntity(current);
        if (!(startBE instanceof IImmersiveConnectable startConn)) return;

        // Small BFS over IE connectables along ONLY our wire type,
        // stopping when we hit CableNetwork.isCable blocks.
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        LongOpenHashSet seen = new LongOpenHashSet();

        q.add(current);
        seen.add(current.asLong());

        // Safety cap to avoid pathological graphs; adjust if you want
        int expansionsLeft = 256;

        while (!q.isEmpty() && expansionsLeft-- > 0) {
            BlockPos at = q.removeFirst();

            BlockEntity be = level.getBlockEntity(at);
            if (!(be instanceof IImmersiveConnectable conn)) continue;

            // Iterate all connection points (relays often have multiple)
            for (ConnectionPoint cp : conn.getConnectionPoints()) {
                LocalWireNetwork local = global.getNullableLocalNet(cp);
                if (local == null) continue;

                for (Connection c : local.getConnections(cp)) {
                    if (c.type != MoreImmersiveWires.SFM_WIRE.simple().wireType) continue;

                    ConnectionPoint otherCp = c.getEndA().equals(cp) ? c.getEndB() : c.getEndA();
                    BlockPos otherPos = otherCp.position();
                    if (otherPos.equals(current)) continue;

                    // If the other end is a cable, emit it as a neighbor.
                    // Otherwise, if it's a relay/connector (connectable), traverse through it.
                    if (CableNetwork.isCable(level, otherPos)) {
                        out.accept(otherPos.immutable());
                    } else {
                        // Only traverse through IE connectable
                        BlockEntity otherBE = level.getBlockEntity(otherPos);
                        if (otherBE instanceof IImmersiveConnectable) {
                            long key = otherPos.asLong();
                            if (seen.add(key)) q.add(otherPos);
                        }
                    }
                }
            }
        }
    }
}
