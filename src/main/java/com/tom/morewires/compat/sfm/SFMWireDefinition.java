package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import com.tom.morewires.SimpleWireTypeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SFMWireDefinition extends SimpleWireTypeDefinition<SFMConnectorBlockEntity> {
    public SFMWireDefinition() {
        super("sfm", "SFM Cable", 0x17161f);
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
