package com.tom.morewires.compat.mi;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.network.NodeNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MILvNetworkHandler extends NodeNetworkHandler<BlockPos, BlockPos> {
    protected MILvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override protected void clearConnection(BlockPos pos) {}
    @Override protected BlockPos getNode() { return BlockPos.ZERO; }
    @Override protected void connectFirst(IImmersiveConnectable iic, BlockPos ignoredMain) {}
    @Override protected BlockPos connect(IImmersiveConnectable iic, BlockPos main) { return iic.getPosition(); }

    @Override public void update(Level level) {
        super.update(level);
        // NO energy logic here when using MI PipeBlockEntity
    }
}