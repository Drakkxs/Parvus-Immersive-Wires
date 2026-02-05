package com.tom.morewires.compat.mi.util;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class MIPipeHook {
    private MIPipeHook() {}

    /** Called when your connector is placed or changes in a way that should cause MI pipes to connect. */
    public static void rescanAdjacentPipes(Level level, BlockPos connectorPos, CableTier connectorTier) {
        if (level.isClientSide) return;

        for (Direction dir : Direction.values()) {
            BlockPos pipePos = connectorPos.relative(dir);
            var be = level.getBlockEntity(pipePos);
            if (!(be instanceof PipeBlockEntity pipeBE)) continue;

            Direction pipeToConnector = dir.getOpposite();

            for (PipeNetworkNode node : pipeBE.getNodes()) {
                PipeNetworkType type = node.getType();

                CableTier pipeTier = MIPipes.ELECTRICITY_PIPE_TIER.get(type);
                if (pipeTier == null || pipeTier != connectorTier) continue;

                // This is the supported way to create a connection.
                pipeBE.addConnection(null, type, pipeToConnector);
            }

            pipeBE.setChanged();
        }
    }

    /** Called when your connector is removed (or stops exposing energy) so MI pipes disconnect. */
    public static void removeAdjacentElectricConnections(Level level, BlockPos connectorPos) {
        if (level.isClientSide) return;

        for (Direction dir : Direction.values()) {
            BlockPos pipePos = connectorPos.relative(dir);
            var be = level.getBlockEntity(pipePos);
            if (!(be instanceof PipeBlockEntity pipeBE)) continue;

            Direction pipeToConnector = dir.getOpposite();
            boolean changed = false;

            for (PipeNetworkNode node : pipeBE.getNodes()) {
                PipeNetworkType type = node.getType();
                if (!MIPipes.ELECTRICITY_PIPE_TIER.containsKey(type)) continue;

                // Optional guard: only remove if the pipe thinks it's connected that side
                PipeEndpointType[] conns = node.getConnections(pipeBE.getBlockPos());
                if (conns == null) continue;

                PipeEndpointType endpoint = conns[pipeToConnector.get3DDataValue()];
                if (endpoint == null) continue;

                pipeBE.removeConnection(type, pipeToConnector);
                changed = true;
            }

            if (changed) {
                pipeBE.setChanged();
            }
        }
    }
}