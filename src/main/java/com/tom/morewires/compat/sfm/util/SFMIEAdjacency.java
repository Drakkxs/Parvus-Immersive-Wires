package com.tom.morewires.compat.sfm.util;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import ca.teamdman.sfm.common.util.SFMDirections;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.sfm.SFMConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public final class SFMIEAdjacency {
    private SFMIEAdjacency() {}

    public static void forEachCableNeighbor(Level level, BlockPos current, Consumer<BlockPos> out) {
        // 6 physical neighbors
        BlockPos.MutableBlockPos tmp = new BlockPos.MutableBlockPos();
        for (Direction d : SFMDirections.DIRECTIONS_WITHOUT_NULL) {
            tmp.set(current).move(d);
            out.accept(tmp.immutable());
        }

        // Wire neighbors only when current is an SFM connector
        BlockEntity be = level.getBlockEntity(current);
        if (!(be instanceof SFMConnectorBlockEntity)) return;

        GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
        if (global == null) return;

        ConnectionPoint cp = new ConnectionPoint(current, 0);
        LocalWireNetwork local = global.getNullableLocalNet(cp);
        if (local == null) return;

        for (Connection conn : local.getConnections(cp)) {
            // Mapping-dependent: if this doesn't compile, switch to conn.getType() / conn.type()
            if (conn.type != MoreImmersiveWires.SFM_WIRE.simple().wireType) continue;

            ConnectionPoint other = conn.getEndA().equals(cp) ? conn.getEndB() : conn.getEndA();
            out.accept(other.position());
        }
    }
}
