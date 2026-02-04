package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.network.SimpleNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MILvNetworkHandler extends SimpleNetworkHandler<MILvConnectorBlockEntity, MILvNetworkHandler> {
    private static final CableTier TIER = CableTier.LV;

    public MILvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MILvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return (iic instanceof MILvConnectorBlockEntity be) ? be : null;
    }

    @Override
    protected void setNetworkHandler(MILvConnectorBlockEntity c, MILvNetworkHandler handler) {
        // optional: store backref in BE later
    }

    @Override
    public void update(Level level) {
        super.update(level);
        if (level.isClientSide) return;

        // 1) gather connectors
        List<MILvConnectorBlockEntity> connectors = new ArrayList<>();
        visitAll(connectors::add);
        if (connectors.isEmpty()) return;

        long maxTransfer = TIER.getMaxTransfer();

        // 2) compute networkAmount + capacity (MI-style)
        long networkAmount = 0;
        for (var c : connectors) networkAmount += c.getStored();

        long networkCapacity = (long) connectors.size() * maxTransfer;

        // 3) gather adjacent storages
        List<MIEnergyStorage> storages = new ArrayList<>();
        for (var conn : connectors) {
            BlockPos pos = conn.getBlockPos();
            for (Direction dir : Direction.values()) {
                Direction port = conn.getFacing().getOpposite();
                BlockPos adj = pos.relative(port);
                MIEnergyStorage st = level.getCapability(EnergyApi.SIDED, adj, port.getOpposite());
                if (st == null) continue;
                if (!st.canConnect(TIER)) continue;

                // Skip our own connector BE storage if it ever comes back
                if (st == conn.getExposedEnergy()) continue;
                if (!storages.contains(st)) storages.add(st);
            }
        }

        if (storages.isEmpty()) {
            // still rebalance to keep connector values sane
            rebalance(connectors, networkAmount);
            return;
        }

        // Split providers/consumers
        List<MIEnergyStorage> providers = new ArrayList<>();
        List<MIEnergyStorage> consumers = new ArrayList<>();
        for (var s : storages) {
            if (s.canExtract()) providers.add(s);
            if (s.canReceive()) consumers.add(s);
        }

        // 4) extract into network pool (cap by remaining capacity + tier maxTransfer like MI)
        if (!providers.isEmpty()) {
            long extractMax = Math.min(maxTransfer, networkCapacity - networkAmount);
            if (extractMax > 0) {
                long extracted = transferForTargets(MIEnergyStorage::extract, providers, extractMax);
                networkAmount += extracted;
            }
        }

        // 5) insert out of pool
        if (!consumers.isEmpty()) {
            long insertMax = Math.min(maxTransfer, networkAmount);
            if (insertMax > 0) {
                long inserted = transferForTargets(MIEnergyStorage::receive, consumers, insertMax);
                networkAmount -= inserted;
            }
        }

        // 6) rebalance remaining EU evenly across connectors (MI-style)
        rebalance(connectors, networkAmount);
    }

    private void rebalance(List<MILvConnectorBlockEntity> connectors, long networkAmount) {
        // Even distribution, MI style
        long remaining = networkAmount;
        int remainingCount = connectors.size();

        for (var c : connectors) {
            long share = remainingCount > 0 ? (remaining / remainingCount) : 0;
            c.setStored(share);
            remaining -= share;
            remainingCount--;
        }
    }

    @FunctionalInterface
    private interface TransferOp {
        long transfer(MIEnergyStorage target, long maxAmount, boolean simulate);
    }

    private static long transferForTargets(TransferOp op, List<MIEnergyStorage> targets, long maxAmount) {
        if (maxAmount <= 0 || targets.isEmpty()) return 0;

        // Shuffle to avoid bias like MI
        Collections.shuffle(targets);

        // Simulate
        long[] sim = new long[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            sim[i] = op.transfer(targets.get(i), maxAmount, true);
        }

        // Sort indices by sim result (low to high)
        Integer[] idx = new Integer[targets.size()];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, java.util.Comparator.comparingLong(i -> sim[i]));

        // Perform fair split
        long transferred = 0;
        for (int k = 0; k < idx.length; k++) {
            int i = idx[k];
            int remainingTargets = idx.length - k;
            long remainingAmount = maxAmount - transferred;
            long quota = remainingAmount / remainingTargets;
            transferred += op.transfer(targets.get(i), quota, false);
        }

        return transferred;
    }
}