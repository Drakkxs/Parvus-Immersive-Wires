package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import com.tom.morewires.network.SimpleNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public abstract class MINetworkHandler<T extends MIConnectorBlockEntityBase>
        extends SimpleNetworkHandler<T, MINetworkHandler<T>>
        implements IWorldTickable {

    protected MINetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected void setNetworkHandler(T c, MINetworkHandler<T> handler) {
        // no-op
    }

    @Override
    public void update(Level level) {
        super.update(level);
        if (level instanceof ServerLevel server) {
            tickNetwork(server);
        }
    }

    private void tickNetwork(ServerLevel level) {
        // 1) snapshot connectors
        List<T> connectors = new ArrayList<>();
        visitAll(c -> {
            if (c != null && !c.isRemoved() && c.getLevel() == level) connectors.add(c);
        });
        if (connectors.isEmpty()) return;

        CableTier tier = connectors.get(0).getCableTier();
        long maxTransfer = tier.getMaxTransfer();

        // 2) sum pool
        long networkAmount = 0;
        for (var c : connectors) networkAmount += c.getNodeEu();
        long networkCapacity = (long) connectors.size() * maxTransfer;

        // 3) gather adjacent storages at port
        List<MIEnergyStorage> extractors = new ArrayList<>();
        List<MIEnergyStorage> receivers = new ArrayList<>();
        List<MIEnergyStorage> both = new ArrayList<>();
        Set<MIEnergyStorage> dedupe = Collections.newSetFromMap(new IdentityHashMap<>());

        for (var conn : connectors) {
            Direction port = conn.getFacing();
            BlockPos adj = conn.getBlockPos().relative(port);

            MIEnergyStorage st = level.getCapability(EnergyApi.SIDED, adj, port.getOpposite());
            if (st == null) continue;
            if (!st.canConnect(tier)) continue;
            if (!dedupe.add(st)) continue;

            boolean ex = st.canExtract();
            boolean rc = st.canReceive();
            if (ex && rc) both.add(st);
            else if (ex) extractors.add(st);
            else if (rc) receivers.add(st);
        }

        // prevent pull+push to same storage in one tick
        Set<MIEnergyStorage> used = Collections.newSetFromMap(new IdentityHashMap<>());

        // Pull
        long room = networkCapacity - networkAmount;
        long pullMax = Math.min(maxTransfer, room);
        if (pullMax > 0) {
            if (!extractors.isEmpty()) {
                long got = transferForTargets((t, amt, sim) -> t.extract(amt, sim), extractors, pullMax);
                if (got > 0) used.addAll(extractors);
                networkAmount += got;
                pullMax -= got;
            }
            if (pullMax > 0 && !both.isEmpty()) {
                List<MIEnergyStorage> tmp = new ArrayList<>(both);
                tmp.removeIf(used::contains);
                long got = transferForTargets((t, amt, sim) -> t.extract(amt, sim), tmp, pullMax);
                networkAmount += got;
            }
        }

        // Push
        long pushMax = Math.min(maxTransfer, networkAmount);
        if (pushMax > 0) {
            if (!receivers.isEmpty()) {
                long put = transferForTargets((t, amt, sim) -> t.receive(amt, sim), receivers, pushMax);
                if (put > 0) used.addAll(receivers);
                networkAmount -= put;
                pushMax -= put;
            }
            if (pushMax > 0 && !both.isEmpty()) {
                List<MIEnergyStorage> tmp = new ArrayList<>(both);
                tmp.removeIf(used::contains);
                long put = transferForTargets((t, amt, sim) -> t.receive(amt, sim), tmp, pushMax);
                networkAmount -= put;
            }
        }

        // Rebalance across connector buffers
        long remaining = networkAmount;
        int remainingCount = connectors.size();
        for (var c : connectors) {
            long share = remainingCount > 0 ? (remaining / remainingCount) : 0;
            c.setNodeEu(share);
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

        Collections.shuffle(targets);

        long[] sim = new long[targets.size()];
        for (int i = 0; i < targets.size(); i++) sim[i] = op.transfer(targets.get(i), maxAmount, true);

        Integer[] idx = new Integer[targets.size()];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingLong(i -> sim[i]));

        long transferred = 0;
        for (int k = 0; k < idx.length; k++) {
            int i = idx[k];
            int remainingTargets = idx.length - k;
            long remainingAmount = maxAmount - transferred;
            long quota = remainingAmount / remainingTargets;
            if (quota <= 0) continue;
            transferred += op.transfer(targets.get(i), quota, false);
        }
        return transferred;
    }
}