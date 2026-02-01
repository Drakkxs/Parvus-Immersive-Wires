package com.parvus.morewires.compat.sfm;

import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.parvus.morewires.MoreImmersiveWires;
import com.parvus.morewires.SimpleWireTypeDefinition;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;

public class SFMWireDefinition extends SimpleWireTypeDefinition<SFMConnectorBlockEntity> {

    public SFMWireDefinition() {
        super("sfm", "SFM Inventory Cable", 0x757575);
    }

    @Override
    public SFMConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        return new SFMConnectorBlockEntity(MoreImmersiveWires.SFM_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos) instanceof ICableBlock;
    }

    @Override
    protected ILocalHandlerConstructor createLocalHandler() {
        return SFMNetworkHandler::new;
    }

}