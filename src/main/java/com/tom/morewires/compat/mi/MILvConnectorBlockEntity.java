package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.collect.ImmutableList;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.tile.IOnCable.IOnCableConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public class MILvConnectorBlockEntity extends BlockEntity implements MIEnergyStorage, IOnCableConnector {

	protected GlobalWireNetwork globalNet;
	private static final CableTier TIER = CableTier.LV;

	// 2 seconds buffer
	private long stored = 0;

	// Wrapper exposed to MI capability system (Jade-friendly)
	private final MIEnergyStorage exposed = new MIEnergyStorage() {
		@Override public boolean canConnect(CableTier tier) { return tier == TIER; }
		@Override public long receive(long maxReceive, boolean simulate) { return MILvConnectorBlockEntity.this.receive(maxReceive, simulate); }
		@Override public long extract(long maxExtract, boolean simulate) { return MILvConnectorBlockEntity.this.extract(maxExtract, simulate); }
		@Override public long getAmount() { return MILvConnectorBlockEntity.this.getAmount(); }
		@Override public long getCapacity() { return MILvConnectorBlockEntity.this.getCapacity(); }
		@Override public boolean canReceive() { return MILvConnectorBlockEntity.this.canReceive(); }
		@Override public boolean canExtract() { return MILvConnectorBlockEntity.this.canExtract(); }
	};

	public MILvConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(MoreImmersiveWires.MI_LV_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
	}

	public MIEnergyStorage getExposedEnergy() {
		return exposed;
	}

	public long getStored() {
		clamp();
		return stored;
	}

	public void setStored(long value) {
		stored = value;
		clamp();
		setChanged();
	}

	private long maxTransfer() {
		return TIER.getMaxTransfer();
	}

	private long capacity() {
		return maxTransfer() * 40;
	}

	private void clamp() {
		if (stored < 0) stored = 0;
		long cap = capacity();
		if (stored > cap) stored = cap;
	}

	// ---- MIEnergyStorage (internal) ----

	@Override
	public boolean canConnect(CableTier cableTier) {
		return cableTier == TIER;
	}

	@Override
	public long getAmount() {
		clamp();
		return stored;
	}

	@Override
	public long getCapacity() {
		return capacity();
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public long receive(long amount, boolean simulate) {

		if (amount <= 0) return 0;
		clamp();

		long toAdd = Math.min(amount, maxTransfer());
		long space = capacity() - stored;
		long added = Math.min(toAdd, Math.max(0, space));

		if (!simulate && added > 0) {
			stored += added;
			setChanged();
//			if (level != null) level.invalidateCapabilities(worldPosition);
		}
		return added;
	}

	@Override
	public long extract(long amount, boolean simulate) {
		if (amount <= 0) return 0;
		clamp();

		long toTake = Math.min(amount, maxTransfer());
		long taken = Math.min(toTake, stored);

		if (!simulate && taken > 0) {
			stored -= taken;
			setChanged();
//			if (level != null) level.invalidateCapabilities(worldPosition);
		}
		return taken;
	}

	// ---- NBT ----
	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		clamp();
		tag.putLong("Energy", stored);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		stored = tag.getLong("Energy");
		clamp();
	}

	// ---- IE connector glue ----
	@Override public boolean canConnect() { return true; }

	@Override
	public boolean canConnectCable(WireType wireType, ConnectionPoint target, Vec3i offset) {
		if (level == null || level.isClientSide) return false;
		if (wireType != MoreImmersiveWires.MI_LV_WIRE.simple().wireType) return false;

		GlobalWireNetwork net = GlobalWireNetwork.getNetwork(level);
		if (net == null) return true;

		ConnectionPoint here = new ConnectionPoint(worldPosition, 0);
		LocalWireNetwork local = net.getNullableLocalNet(here);
		return local == null || local.getConnections(here).isEmpty();
	}

	@Override public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) { setChanged(); }
	@Override public void removeCable(Connection connection, ConnectionPoint attachedPoint) { setChanged(); }

	@Override public BlockPos getPosition() { return worldPosition; }
	@Override public Level getLevelNonnull() { return level; }
	@Override public BlockState getState() { return getBlockState(); }

	@Override
	public Collection<ResourceLocation> getRequestedHandlers() {
		return ImmutableList.of(MoreImmersiveWires.MI_LV_WIRE.simple().NET_ID);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide) {
			globalNet = GlobalWireNetwork.getNetwork(level);
			if (globalNet != null) ConnectorBlockEntityHelper.onChunkLoad(this, level);
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (globalNet != null) ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null && !level.isClientSide && globalNet != null) {
			ConnectorBlockEntityHelper.remove(level, this);
		}
	}
}