package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import com.google.common.collect.ImmutableList;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.tile.IOnCable.IOnCableConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MILvConnectorBlockEntity extends BlockEntity
		implements MIEnergyStorage, IOnCableConnector, IEBlockInterfaces.IStateBasedDirectional {

	public MILvConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(MoreImmersiveWires.MI_LV_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
	}


	private static final CableTier TIER = CableTier.LV;

	private long eu = 0;

	private final MIEnergyStorage exposed = new MIEnergyStorage() {
		@Override public boolean canConnect(CableTier tier) { return tier == TIER; }
		@Override public long receive(long maxReceive, boolean simulate) { return MILvConnectorBlockEntity.this.receive(maxReceive, simulate); }
		@Override public long extract(long maxExtract, boolean simulate) { return MILvConnectorBlockEntity.this.extract(maxExtract, simulate); }
		@Override public long getAmount() { return MILvConnectorBlockEntity.this.getAmount(); }
		@Override public long getCapacity() { return MILvConnectorBlockEntity.this.getCapacity(); }
		@Override public boolean canReceive() { return true; }
		@Override public boolean canExtract() { return true; }
	};

	protected GlobalWireNetwork globalNet;
	private boolean isUnloaded = false;

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		globalNet = GlobalWireNetwork.getNetwork(level);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide) {
			ConnectorBlockEntityHelper.onChunkLoad(this, level);
		}
		isUnloaded = false;
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (level != null && !level.isClientSide && globalNet != null) {
			ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
		}
		isUnloaded = true;
	}

	private void setRemovedIE() {
		if (level != null && !level.isClientSide && globalNet != null) {
			ConnectorBlockEntityHelper.remove(level, this);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (!isUnloaded) setRemovedIE();
	}

	// in MILvConnectorBlockEntity
	private long lastNetworkEu;
	private long lastNetworkCap;

	public void setNetworkInfo(long eu, long cap) {
		this.lastNetworkEu = eu;
		this.lastNetworkCap = cap;
	}

	public long getNetworkStoredEu() { return lastNetworkEu; }
	public long getNetworkCapacityEu() { return lastNetworkCap; }

	public MIEnergyStorage getExposedEnergy() {
		return exposed;
	}

	@Override
	public Level getLevelNonnull() {
		return level;
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers() {
		return ImmutableList.of(MoreImmersiveWires.MI_LV_WIRE.simple().NET_ID);
	}

	// --- Facing (IE) ---
	@Override
	public Property<Direction> getFacingProperty() {
		return MILvConnectorBlock.FACING;
	}

	@Override
	public void setState(BlockState state) {
		if (level != null) level.setBlock(worldPosition, state, 3);
	}

	@Override
	public boolean canConnect() {
		return true;
	}

	@Override
	public boolean canConnectCable(WireType wireType, ConnectionPoint target, Vec3i offset) {
		if (level == null || level.isClientSide) return false;
		return wireType == MoreImmersiveWires.MI_LV_WIRE.simple().wireType;
	}

	@Override
	public void connectCable(WireType type, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
		setChanged();
	}

	@Override
	public void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint) {
		setChanged();
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
	public boolean canConnect(CableTier cableTier) {
		return cableTier == TIER;
	}

	@Override
	public long getAmount() {
		return eu;
	}

	@Override
	public long getCapacity() {
		return TIER.getMaxTransfer(); // MI pipes: capacity == maxTransfer per node
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	public long getNodeEu() { return eu; }

	public void setNodeEu(long value) {
		eu = Math.max(0, Math.min(value, getCapacity()));
		setChanged();
	}

	@Override
	public long receive(long maxReceive, boolean simulate) {
		if (maxReceive <= 0) return 0;
		long space = getCapacity() - eu;
		long moved = Math.min(maxReceive, Math.max(0, space));
		if (!simulate && moved > 0) {
			eu += moved;
			setChanged();
		}
		return moved;
	}

	@Override
	public long extract(long maxExtract, boolean simulate) {
		if (maxExtract <= 0) return 0;
		long moved = Math.min(maxExtract, eu);
		if (!simulate && moved > 0) {
			eu -= moved;
			setChanged();
		}
		return moved;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putLong("eu", eu);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		eu = tag.getLong("eu");
		if (eu < 0) eu = 0;
		long cap = getCapacity();
		if (eu > cap) eu = cap;
	}
}