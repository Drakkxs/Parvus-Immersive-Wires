package com.tom.morewires.compat.sfm;

import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.morewires.SimpleWireTypeDefinition;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;

public class SFMWireDefinition extends SimpleWireTypeDefinition<SFMConnectorBlockEntity> {
    public SFMWireDefinition() {
        super("sfm", "SFM Cable", 0xFF6600);
    }

    @Override
    protected ILocalHandlerConstructor createLocalHandler() {
        return SFMNetworkHandler::new;
    }

    @Override
    public SFMConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        return new SFMConnectorBlockEntity(pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        // Check if block is SFM cable
        return level.getBlockState(pos).getBlock() instanceof ICableBlock;
    }

}
