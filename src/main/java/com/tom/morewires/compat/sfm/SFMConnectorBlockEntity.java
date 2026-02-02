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

		// Only allow our wire type
		if (cableType != MoreImmersiveWires.SFM_WIRE.simple().wireType) return false;

		// IMPORTANT: don't rely on cached globalNet during early load
		GlobalWireNetwork net = this.globalNet;
		if (net == null) net = GlobalWireNetwork.getNetwork(level);
		if (net == null) return false;

		ConnectionPoint cp = new ConnectionPoint(this.worldPosition, 0);
		LocalWireNetwork local = net.getNullableLocalNet(cp);

		// Disallow if we already have any connection at this point
		if (local != null && !local.getConnections(cp).isEmpty()) return false;

		return true;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
		if (level == null || level.isClientSide) return;

		// Refresh topology only if this block is actually a cable in SFM terms
		if (level.getBlockState(worldPosition).getBlock() instanceof ICableBlock) {
			CableNetworkManager.onCablePlaced(level, worldPosition);
		}

		// Optional but harmless safety: refresh the other end too
		BlockPos otherPos = other.getPosition();
		if (level.getBlockState(otherPos).getBlock() instanceof ICableBlock) {
			CableNetworkManager.onCablePlaced(level, otherPos);
		}
	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
		if (level == null || level.isClientSide) return;

		if (level.getBlockState(worldPosition).getBlock() instanceof ICableBlock) {
			CableNetworkManager.onCableRemoved(level, worldPosition);
		}

		// Optional safety: call on the other endpoint as well
		BlockPos otherPos = attachedPoint.position();
		if (level.getBlockState(otherPos).getBlock() instanceof ICableBlock) {
			CableNetworkManager.onCableRemoved(level, otherPos);
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
