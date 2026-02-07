package com.tom.morewires.compat.mi.HV;

import com.tom.morewires.compat.mi.MIConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class MIHvConnectorBlock extends MIConnectorBlock<MIHvConnectorBlockEntity> {
    public MIHvConnectorBlock(
            DeferredHolder<BlockEntityType<?>, BlockEntityType<MIHvConnectorBlockEntity>> type,
            BiPredicate<BlockGetter, BlockPos> isOnCable
    ) {
        super(type, isOnCable);
    }
}