package com.tom.morewires.compat.sfm;

import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import com.tom.morewires.block.OnCableConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class SFMConnectorBlock extends OnCableConnectorBlock<SFMConnectorBlockEntity> implements ICableBlock {

	public SFMConnectorBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<SFMConnectorBlockEntity>> type,
							 BiPredicate<BlockGetter, BlockPos> isOnCable) {
		super(type, isOnCable);
	}

}
