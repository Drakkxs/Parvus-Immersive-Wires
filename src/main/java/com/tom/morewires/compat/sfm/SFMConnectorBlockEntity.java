package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.*;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
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

public class SFMConnectorBlockEntity extends BlockEntity implements IOnCableConnector {
	protected GlobalWireNetwork globalNet;

	public SFMConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(MoreImmersiveWires.SFM_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
	}

	@Override
	public boolean canConnect() {
		return true;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
		if (level == null) return false;

		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(level);
		if (net == null) return false;

		LocalWireNetwork local = net.getNullableLocalNet(new ConnectionPoint(this.worldPosition, 0));
		if (local != null && !local.getConnections(this.worldPosition).isEmpty())
			return false;

		return cableType == MoreImmersiveWires.SFM_WIRE.simple().wireType;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
		// Trigger SFM network refresh when wire connects
		if (level != null) {
			CableNetworkManager.onCablePlaced(level, worldPosition);
			CableNetworkManager.getOrRegisterNetworkFromCablePosition(level, worldPosition).ifPresent(cableNetwork -> {
				CableNetworkManager.getOrRegisterNetworkFromCablePosition(level, other.getPosition()).ifPresent(cableNetwork::mergeNetwork);
			});
		}
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
		// Trigger SFM network refresh when wire disconnects
		if (level != null) {
			CableNetworkManager.onCableRemoved(level, worldPosition);
			CableNetworkManager.onCableRemoved(level, attachedPoint.position());
		}
	}

	@Override
	public BlockPos getPosition() {
		return worldPosition;
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers() {
		return ImmutableList.of(MoreImmersiveWires.SFM_WIRE.simple().NET_ID);
	}

	@Override
	public Level getLevelNonnull() {
		return level;
	}

	@Override
	public BlockState getState() {
		return getBlockState();
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide) {
			globalNet = GlobalWireNetwork.getNetwork(level);
			if (globalNet != null) {
				ConnectorBlockEntityHelper.onChunkLoad(this, level);
			}
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (globalNet != null) {
			ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null && !level.isClientSide && globalNet != null) {
			ConnectorBlockEntityHelper.remove(level, this);
		}
	}
}
