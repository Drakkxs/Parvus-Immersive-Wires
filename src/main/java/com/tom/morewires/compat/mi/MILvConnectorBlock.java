package com.tom.morewires.compat.mi;

import com.tom.morewires.block.OnCableConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class MILvConnectorBlock extends OnCableConnectorBlock<MILvConnectorBlockEntity> {


    public MILvConnectorBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type, BiPredicate<BlockGetter, BlockPos> isOnCable) {
        super(type, isOnCable);
    }
}