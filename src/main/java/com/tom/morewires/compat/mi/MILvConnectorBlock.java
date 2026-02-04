package com.tom.morewires.compat.mi;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Supplier;

public class MILvConnectorBlock extends ConnectorBlock<MILvConnectorBlockEntity> {
	public static final DirectionProperty FACING = IEProperties.FACING_ALL;

	public MILvConnectorBlock(Properties props, Supplier<BlockEntityType<MILvConnectorBlockEntity>> entityType) {
		super(props, entityType);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}
}