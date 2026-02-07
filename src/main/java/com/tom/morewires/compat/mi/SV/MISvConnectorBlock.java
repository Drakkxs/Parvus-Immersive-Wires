package com.tom.morewires.compat.mi.SV;

import com.tom.morewires.compat.mi.MIConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class MISvConnectorBlock extends MIConnectorBlock<MISvConnectorBlockEntity> {
    public MISvConnectorBlock(
            DeferredHolder<BlockEntityType<?>, BlockEntityType<MISvConnectorBlockEntity>> type,
            BiPredicate<BlockGetter, BlockPos> isOnCable
    ) {
        super(type, isOnCable);
    }
}