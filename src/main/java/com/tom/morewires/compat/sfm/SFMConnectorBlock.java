package com.tom.morewires.compat.sfm;

import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import ca.teamdman.sfm.common.util.Stored;
import com.tom.morewires.block.OnCableConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class SFMConnectorBlock extends OnCableConnectorBlock<SFMConnectorBlockEntity> implements ICableBlock {

	public SFMConnectorBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<SFMConnectorBlockEntity>> type,
							 BiPredicate<BlockGetter, BlockPos> isOnCable) {
		super(type, isOnCable);
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
			CableNetworkManager.onCablePlaced(world, pos);
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
			CableNetworkManager.onCableRemoved(level, pos);
		}
	}
}
