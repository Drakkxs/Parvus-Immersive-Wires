package com.tom.morewires.compat.mi;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.SimpleWireTypeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MILvWireDefinition extends SimpleWireTypeDefinition<MILvConnectorBlockEntity> {
    public MILvWireDefinition() {
        super("mi_lv", "MI Low Voltage", 0x17161f);
    }

    @Override
    protected ILocalHandlerConstructor createLocalHandler() {
        return MILvNetworkHandler::new;
    }

    @Override
    public MILvConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        return new MILvConnectorBlockEntity(pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof MILvConnectorBlock;
    }
}