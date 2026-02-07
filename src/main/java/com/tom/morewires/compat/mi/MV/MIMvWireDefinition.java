package com.tom.morewires.compat.mi.MV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MIMvWireDefinition extends MIWireDefinitionBase<MIMvConnectorBlockEntity> {

    public MIMvWireDefinition() {
        super("mi_mv", "Modern Medium Voltage", 0x003260);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MIMvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MIMvConnectorBlockEntity>> type) {
        return new MIMvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MIMvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MIMvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MIMvConnectorBlock.class;
    }
}