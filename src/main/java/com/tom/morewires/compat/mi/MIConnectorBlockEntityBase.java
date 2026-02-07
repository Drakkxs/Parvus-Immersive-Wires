package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierHolder;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import com.tom.morewires.compat.mi.util.MIPipeHook;
import com.tom.morewires.tile.IOnCable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class MIConnectorBlockEntityBase extends BlockEntity
        implements MIEnergyStorage, CableTierHolder, IImmersiveConnectable, IOnCable.IOnCableConnector,
        IEBlockInterfaces.IStateBasedDirectional {

    protected final CableTier tier;
    protected final ResourceLocation netId;
    protected final WireType ieWireType;

    protected GlobalWireNetwork globalNet;
    protected long eu = 0;

    protected MIConnectorBlockEntityBase(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            CableTier tier,
            ResourceLocation netId,
            WireType ieWireType
    ) {
        super(type, pos, state);
        this.tier = tier;
        this.netId = netId;
        this.ieWireType = ieWireType;
    }

    // ----- MI tier -----
    @Override public CableTier getCableTier() { return tier; }
    @Override public boolean canConnect(CableTier t) { return t == tier; }

    private final MIEnergyStorage exposed = new MIEnergyStorage() {
        @Override public boolean canConnect(CableTier t) { return t == tier; }
        @Override public long receive(long a, boolean sim) { return MIConnectorBlockEntityBase.this.receive(a, sim); }
        @Override public long extract(long a, boolean sim) { return MIConnectorBlockEntityBase.this.extract(a, sim); }
        @Override public long getAmount() { return MIConnectorBlockEntityBase.this.getAmount(); }
        @Override public long getCapacity() { return MIConnectorBlockEntityBase.this.getCapacity(); }
        @Override public boolean canReceive() { return MIConnectorBlockEntityBase.this.canReceive(); }
        @Override public boolean canExtract() { return MIConnectorBlockEntityBase.this.canExtract(); }
    };
    public MIEnergyStorage getExposedEnergy() { return exposed; }

    @Override public long getCapacity() { return tier.getMaxTransfer(); }
    @Override public long getAmount() { return eu; }
    @Override public boolean canReceive() { return true; }
    @Override public boolean canExtract() { return true; }

    @Override
    public long receive(long maxReceive, boolean simulate) {
        if (maxReceive <= 0) return 0;
        long space = getCapacity() - eu;
        long moved = Math.min(maxReceive, Math.max(0, space));
        if (!simulate && moved > 0) { eu += moved; setChanged(); }
        return moved;
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        if (maxExtract <= 0) return 0;
        long moved = Math.min(maxExtract, eu);
        if (!simulate && moved > 0) { eu -= moved; setChanged(); }
        return moved;
    }

    // ----- Facing -----
    @Override public Property<Direction> getFacingProperty() { return ConnectorBlock.DEFAULT_FACING_PROP; }
    @Override public BlockState getState() { return getBlockState(); }

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
            this.invalidateCapabilities();
            level.updateNeighborsAt(worldPosition, state.getBlock());
            level.updateNeighborsAt(worldPosition.relative(state.getValue(getFacingProperty())), state.getBlock());
            if (!level.isClientSide) {
                MIPipeHook.rescanAdjacentPipes(level, worldPosition, tier);
            }
        }
    }

    // ----- IE connectable -----
    @Override public boolean canConnect() { return true; }
    @Override public BlockPos getPosition() { return worldPosition; }
    @Override public Level getLevelNonnull() { return level; }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return List.of(netId);
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint target, Vec3i offset) {
        return level != null && !level.isClientSide && wireType == ieWireType;
    }

    @Override public void connectCable(WireType t, ConnectionPoint target, IImmersiveConnectable o, ConnectionPoint ot) { setChanged(); }
    @Override public void removeCable(@Nullable Connection c, ConnectionPoint p) { setChanged(); }

    // ----- NBT -----
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("eu", eu);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        eu = Math.max(0, Math.min(tag.getLong("eu"), getCapacity()));
    }

    public long getNodeEu() { return eu; }
    public void setNodeEu(long v) {
        long nv = Math.max(0, Math.min(v, getCapacity()));
        if (nv != eu) { eu = nv; setChanged(); }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && !level.isClientSide) globalNet = GlobalWireNetwork.getNetwork(level);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // Ensure this is initialized here (ordering-safe)
            globalNet = GlobalWireNetwork.getNetwork(level);

            // Let IE rebuild/attach this connector to the wire network
            if (globalNet != null) {
                ConnectorBlockEntityHelper.onChunkLoad(this, level);
            }

            // Your MI stuff is fine to run after
            this.invalidateCapabilities();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            MIPipeHook.rescanAdjacentPipes(level, worldPosition, tier);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (globalNet != null && level != null && !level.isClientSide) {
            ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
        }
        globalNet = null; // IMPORTANT: prevents setRemoved() from nuking connections on unload
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Only remove from the wire network when we *actually* have a live wire network attached.
        // On chunk unload, globalNet will have been nulled above.
        if (level != null && !level.isClientSide && globalNet != null) {
            ConnectorBlockEntityHelper.remove(level, this);
        }
    }
}