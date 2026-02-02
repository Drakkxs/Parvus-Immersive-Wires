package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.collect.ImmutableList;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.sfm.util.SFMTraversalScheduler;
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
	public boolean canConnectCable(WireType wireType, ConnectionPoint target, Vec3i offset) {
		if (level == null || level.isClientSide) return false;

		if (wireType != MoreImmersiveWires.SFM_WIRE.simple().wireType) return false;

		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(level);
		if (net == null) return true; // allow, IE will finalize later

		ConnectionPoint here = new ConnectionPoint(worldPosition, 0);
		LocalWireNetwork local = net.getNullableLocalNet(here);

		// If you want to allow multiple wires, return true here.
		// If you want one connection only:
		return local == null || local.getConnections(here).isEmpty();
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
		if (level == null || level.isClientSide) return;
		System.out.println("[MIW:SFM] connectCable fired at " + worldPosition);
		SFMTraversalScheduler.markDirty(level, worldPosition);
		SFMTraversalScheduler.markDirty(level, other.getPosition());

	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
		if (level == null || level.isClientSide) return;

		SFMTraversalScheduler.markDirty(level, worldPosition);

		// the other side
		ConnectionPoint other = connection.getEndA().position().equals(attachedPoint.position())
				? connection.getEndB()
				: connection.getEndA();
		SFMTraversalScheduler.markDirty(level, other.position());
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
