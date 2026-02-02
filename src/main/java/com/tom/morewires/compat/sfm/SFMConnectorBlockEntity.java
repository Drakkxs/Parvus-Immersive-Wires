package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.*;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import com.google.common.collect.ImmutableList;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.tile.IOnCable.IOnCableConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public class SFMConnectorBlockEntity extends BlockEntity implements IOnCableConnector, ICableBlock {
	protected GlobalWireNetwork globalNet;

	public SFMConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(MoreImmersiveWires.SFM_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
	}

	@Override
	public boolean canConnect() {
		return false;
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
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
		// Trigger SFM network refresh when wire connects
		CableNetworkManager.onCablePlaced(level, worldPosition);
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
		// Trigger SFM network refresh when wire disconnects
		CableNetworkManager.onCableRemoved(level, worldPosition);
	}

	@Override
	public BlockPos getPosition() {
		return null;
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers() {
		return ImmutableList.of(MoreImmersiveWires.SFM_WIRE.simple().NET_ID);
	}

	@Override
	public Level getLevelNonnull() {
		return null;
	}

	@Override
	public BlockState getState() {
		return null;
	}
}
