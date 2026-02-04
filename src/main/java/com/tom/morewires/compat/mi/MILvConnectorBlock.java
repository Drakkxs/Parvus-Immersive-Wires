package com.tom.morewires.compat.mi;

import blusunrize.immersiveengineering.api.IEProperties;
import com.tom.morewires.block.OnCableConnectorBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public class MILvConnectorBlock extends OnCableConnectorBlock<MILvConnectorBlockEntity> {
	public static final DirectionProperty FACING = IEProperties.FACING_ALL;

	public MILvConnectorBlock(
			DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type,
			BiPredicate<BlockGetter, net.minecraft.core.BlockPos> isOnCable
	) {
		super(type, isOnCable);

		// super already defines ON_CABLE + default state for it
		// we add FACING default here
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder); // adds ON_CABLE
		builder.add(FACING);                       // add our facing
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = super.getStateForPlacement(ctx); // sets ON_CABLE correctly
		if (state == null) return null;
		return state.setValue(FACING, ctx.getClickedFace());
	}
}