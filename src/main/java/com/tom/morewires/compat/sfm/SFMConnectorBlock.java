package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import ca.teamdman.sfm.common.util.Stored;
import com.tom.morewires.compat.sfm.util.SFMTraversalScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class SFMConnectorBlock extends ConnectorBlock<SFMConnectorBlockEntity> implements ICableBlock {

	public SFMConnectorBlock(Properties props, Supplier<BlockEntityType<SFMConnectorBlockEntity>> entityType) {
		super(props, entityType);
	}

	@Override
	public void onPlace(
			BlockState state,
			Level world,
			@Stored BlockPos pos,
			BlockState oldState,
			boolean isMoving
	) {
		// does nothing but keeping for symmetry
		super.onPlace(state, world, pos, oldState, isMoving);

		if (!(oldState.getBlock() instanceof ICableBlock)) {
			SFMTraversalScheduler.markDirty(world, pos);
		}
	}

	@Override
	public void onRemove(
			BlockState state,
			Level level,
			@Stored BlockPos pos,
			BlockState newState,
			boolean isMoving
	) {
		// purges block entity
		super.onRemove(state, level, pos, newState, isMoving);

		if (!(newState.getBlock() instanceof ICableBlock)) {
			SFMTraversalScheduler.markDirty(level, pos);
		}
	}
}
