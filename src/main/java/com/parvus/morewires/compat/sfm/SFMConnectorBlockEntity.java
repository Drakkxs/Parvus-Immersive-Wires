package com.parvus.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.collect.ImmutableList;
import com.parvus.morewires.MoreImmersiveWires;
import com.parvus.morewires.tile.IOnCable.IOnCableConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public class SFMConnectorBlockEntity extends BlockEntity implements IOnCableConnector {
	protected GlobalWireNetwork globalNet;

	public SFMConnectorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
		super(blockEntityType, pos, blockState);
	}

	@Override
	public void setLevel(Level worldIn) {
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Override
	public boolean canConnect() {
		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other,
							 ConnectionPoint otherTarget) {

	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
		LocalWireNetwork local = this.globalNet.getNullableLocalNet(new ConnectionPoint(this.worldPosition, 0));
		if (local != null && !local.getConnections(this.worldPosition).isEmpty()) {
			return false;
		}
		return cableType == MoreImmersiveWires.SFM_WIRE.simple().wireType;
	}

	@Override
	public BlockPos getPosition() {
		return worldPosition;
	}

	@Override
	public BlockState getState() {
		return getBlockState();
	}

	@Override
	public Level getLevelNonnull() {
		return level;
	}

	private boolean isUnloaded = false;

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
		isUnloaded = true;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		ConnectorBlockEntityHelper.onChunkLoad(this, level);
		isUnloaded = false;
	}

	public void setRemovedIE() {
		ConnectorBlockEntityHelper.remove(level, this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(!isUnloaded)
			setRemovedIE();
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers() {
		return ImmutableList.of(MoreImmersiveWires.SFM_WIRE.simple().NET_ID);
	}
}