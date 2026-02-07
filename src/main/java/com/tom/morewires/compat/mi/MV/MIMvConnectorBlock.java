package com.tom.morewires.compat.mi.MV;

import com.tom.morewires.compat.mi.MIConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class MIMvConnectorBlock extends MIConnectorBlock<MIMvConnectorBlockEntity> {
    public MIMvConnectorBlock(
            DeferredHolder<BlockEntityType<?>, BlockEntityType<MIMvConnectorBlockEntity>> type,
            BiPredicate<BlockGetter, BlockPos> isOnCable
    ) {
        super(type, isOnCable);
    }
}