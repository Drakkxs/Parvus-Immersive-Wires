package com.tom.morewires.compat.mi.LV;

import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.compat.mi.MIWireDefinitionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MILvWireDefinition extends MIWireDefinitionBase<MILvConnectorBlockEntity> {

    public MILvWireDefinition() {
        super("mi_lv", "Modern Low Voltage", 0x17161f);
    }

    @Override
    protected ILocalHandlerConstructor createMIHandler() {
        return MILvNetworkHandler::new;
    }

    @Override
    protected Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type) {
        return new MILvConnectorBlock(type, this::isCable);
        // or: return new MIConnectorBlock<>(type, this::isCable); if you went generic there
    }

    @Override
    protected MILvConnectorBlockEntity createMIBE(BlockPos pos, BlockState state) {
        return new MILvConnectorBlockEntity(pos, state);
    }

    @Override
    protected Class<? extends Block> getCableBlockClass() {
        return MILvConnectorBlock.class;
    }
}