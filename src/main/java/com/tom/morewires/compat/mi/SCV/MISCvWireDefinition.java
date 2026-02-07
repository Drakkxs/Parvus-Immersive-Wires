package com.tom.morewires.compat.mi.SCV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MISCvWireDefinition extends MIWireDefinitionBase<MISCvConnectorBlockEntity> {

    public MISCvWireDefinition() {
        super("mi_superv", "Modern Superconductor", 0x6ee7ff);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MISCvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MISCvConnectorBlockEntity>> type) {
        return new MISCvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MISCvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MISCvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MISCvConnectorBlock.class;
    }
}