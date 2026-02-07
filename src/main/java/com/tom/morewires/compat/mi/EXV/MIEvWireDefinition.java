package com.tom.morewires.compat.mi.EXV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MIEvWireDefinition extends MIWireDefinitionBase<MIEvConnectorBlockEntity> {

    public MIEvWireDefinition() {
        super("mi_ev", "Modern Extreme Voltage", 0x009eef);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MIEvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MIEvConnectorBlockEntity>> type) {
        return new MIEvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MIEvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MIEvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MIEvConnectorBlock.class;
    }
}