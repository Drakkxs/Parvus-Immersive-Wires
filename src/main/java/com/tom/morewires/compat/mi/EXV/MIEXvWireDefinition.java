package com.tom.morewires.compat.mi.EXV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MIEXvWireDefinition extends MIWireDefinitionBase<MIEXvConnectorBlockEntity> {

    public MIEXvWireDefinition() {
        super("mi_exv", "Modern Extreme Voltage", 0x17161f);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MIEXvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MIEXvConnectorBlockEntity>> type) {
        return new MIEXvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MIEXvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MIEXvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MIEXvConnectorBlock.class;
    }
}