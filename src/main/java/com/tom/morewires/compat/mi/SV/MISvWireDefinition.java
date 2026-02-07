package com.tom.morewires.compat.mi.SV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MISvWireDefinition extends MIWireDefinitionBase<MISvConnectorBlockEntity> {

    public MISvWireDefinition() {
        super("mi_sv", "Modern Superconductor", 0xefefef);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MISvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MISvConnectorBlockEntity>> type) {
        return new MISvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MISvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MISvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MISvConnectorBlock.class;
    }
}