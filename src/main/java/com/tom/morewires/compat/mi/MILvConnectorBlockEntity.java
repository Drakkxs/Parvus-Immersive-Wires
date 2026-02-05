package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierHolder;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.util.MIPipeHook;
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
import java.util.List;

public class MILvConnectorBlockEntity extends BlockEntity
        implements MIEnergyStorage, CableTierHolder, IImmersiveConnectable, IOnCableConnector,
        IEBlockInterfaces.IStateBasedDirectional {

    private static final CableTier TIER = CableTier.LV;

    private GlobalWireNetwork globalNet;

    private long eu = 0; // internal buffer

    public MILvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(MoreImmersiveWires.MI_LV_WIRE.simple().CONNECTOR_ENTITY.get(), pos, state);
    }

    // ----- MI tier -----
    @Override
    public CableTier getCableTier() {
        return TIER;
    }

    @Override
    public boolean canConnect(CableTier tier) {
        return tier == TIER;
    }

    private final MIEnergyStorage exposed = new MIEnergyStorage() {
        @Override public boolean canConnect(CableTier tier) { return tier == TIER; }
        @Override public long receive(long maxReceive, boolean simulate) { return MILvConnectorBlockEntity.this.receive(maxReceive, simulate); }
        @Override public long extract(long maxExtract, boolean simulate) { return MILvConnectorBlockEntity.this.extract(maxExtract, simulate); }
        @Override public long getAmount() { return MILvConnectorBlockEntity.this.getAmount(); }
        @Override public long getCapacity() { return MILvConnectorBlockEntity.this.getCapacity(); }
        @Override public boolean canReceive() { return MILvConnectorBlockEntity.this.canReceive(); }
        @Override public boolean canExtract() { return MILvConnectorBlockEntity.this.canExtract(); }
    };

    public MIEnergyStorage getExposedEnergy() { return exposed; }

    // Capacity: pick something reasonable.
    // If you want “pipe-like”, using maxTransfer per node is fine:
    @Override
    public long getCapacity() {
        return TIER.getMaxTransfer();
    }

    @Override
    public long getAmount() {
        return eu;
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

    // ----- Facing property (needed by IOnCableConnector bounds + port logic) -----
    @Override
    public Property<Direction> getFacingProperty() {
        return ConnectorBlock.DEFAULT_FACING_PROP; // (IEProperties.FACING_ALL)
    }

    @Override
    public BlockState getState() { return getBlockState(); }

    @Override
    public void setState(BlockState state) {
        if (level == null) return;

        BlockState old = getBlockState();
        boolean facingChanged =
                old.hasProperty(getFacingProperty())
                        && state.hasProperty(getFacingProperty())
                        && old.getValue(getFacingProperty()) != state.getValue(getFacingProperty());

        level.setBlock(worldPosition, state, 3);

        if (facingChanged) {
            // IMPORTANT: capability result depends on facing, so clear side-cache
            this.invalidateCapabilities();

            // Encourage neighbors (MI cables) to re-evaluate connections
            level.updateNeighborsAt(worldPosition, state.getBlock());
            level.updateNeighborsAt(worldPosition.relative(state.getValue(getFacingProperty())), state.getBlock());
            if (!level.isClientSide) {
                MIPipeHook.rescanAdjacentPipes(level, worldPosition, TIER);
            }
        }


    }

    // ----- IE connectable bits (keep what you already had) -----
    @Override public boolean canConnect() { return true; }
    @Override public BlockPos getPosition() { return worldPosition; }
    @Override public Level getLevelNonnull() { return level; }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return List.of(MoreImmersiveWires.MI_LV_WIRE.simple().NET_ID);
    }
    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint target, Vec3i offset) {
        return level != null && !level.isClientSide
                && wireType == MoreImmersiveWires.MI_LV_WIRE.simple().wireType;
    }

    @Override public void connectCable(WireType type, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) { setChanged(); }
    @Override public void removeCable(@Nullable Connection connection, ConnectionPoint attachedPoint) { setChanged(); }

    // ----- NBT -----


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("eu", eu);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        eu = Math.max(0, Math.min(tag.getLong("eu"), getCapacity()));
    }

    public long getNodeEu() {
        return eu;
    }

    public void setNodeEu(long value) {
        long clamped = Math.max(0, Math.min(value, getCapacity()));
        if (clamped != eu) {
            eu = clamped;
            setChanged();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            ConnectorBlockEntityHelper.remove(level, this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && !level.isClientSide) {
            globalNet = GlobalWireNetwork.getNetwork(level);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            this.invalidateCapabilities();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            MIPipeHook.rescanAdjacentPipes(level, worldPosition, TIER);
            ConnectorBlockEntityHelper.onChunkLoad(this, level);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (globalNet != null && level != null && !level.isClientSide) {
            ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
        }
    }

}