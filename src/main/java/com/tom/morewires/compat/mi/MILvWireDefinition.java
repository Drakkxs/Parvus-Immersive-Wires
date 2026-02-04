package com.tom.morewires.compat.mi;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import com.tom.morewires.SimpleWireTypeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MILvWireDefinition extends SimpleWireTypeDefinition<MILvConnectorBlockEntity> {
    public MILvWireDefinition() {
        super("mi_lv", "MI Low Voltage", 0x131313);
    }

    @Override
    protected ILocalHandlerConstructor createLocalHandler() {
        // Your network handler (ticks via IE local handler system)
        return MILvNetworkHandler::new;
    }

    @Override
    public Block makeBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type) {
        return new MILvConnectorBlock(type, this::isCable);
    }

    @Override
    public MILvConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        // Vanilla-style BE ctor: (pos, state)
        return new MILvConnectorBlockEntity(pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        // Whatever block class your connector block is
        return level.getBlockState(pos).getBlock() instanceof MILvConnectorBlock;
    }
}