package com.tom.morewires.compat.mi.HV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MIHvWireDefinition extends MIWireDefinitionBase<MIHvConnectorBlockEntity> {

    public MIHvWireDefinition() {
        super("mi_hv", "Modern High Voltage", 0x5a596b);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MIHvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MIHvConnectorBlockEntity>> type) {
        return new MIHvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MIHvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MIHvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MIHvConnectorBlock.class;
    }
}