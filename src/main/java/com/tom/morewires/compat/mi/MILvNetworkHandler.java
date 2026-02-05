package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import com.tom.morewires.network.SimpleNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class MILvNetworkHandler extends SimpleNetworkHandler<MILvConnectorBlockEntity, MILvNetworkHandler>
        implements IWorldTickable {

    private static final CableTier TIER = CableTier.LV;

    protected MILvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
        System.out.println("[MIW] MILvNetworkHandler constructed");
    }

    @Override
    protected MILvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MILvConnectorBlockEntity be ? be : null;
    }

    @Override
    protected void setNetworkHandler(MILvConnectorBlockEntity c, MILvNetworkHandler handler) {
        // no-op; MI connector doesn't need to store handler reference
    }

    @Override
    public void update(Level level) {
        super.update(level); // keeps internal connection set in sync

        if (!(level instanceof ServerLevel server)) return;

        // optional spam guard
        // if (level.getGameTime() % 20 == 0) System.out.println("[MIW] tick");

        tickNetwork(server);
    }

    private void tickNetwork(ServerLevel level) {
        final long maxTransfer = TIER.getMaxTransfer();

        // 1) snapshot connectors (only loaded ones)
        List<MILvConnectorBlockEntity> connectors = new ArrayList<>();
        visitAll(c -> {
            if (c != null && !c.isRemoved() && c.getLevel() == level) connectors.add(c);
        });
        if (connectors.isEmpty()) return;

        // 2) sum shared pool (your per-node buffers)
        long networkAmount = 0;
        for (var c : connectors) networkAmount += c.getNodeEu();
        long networkCapacity = (long) connectors.size() * maxTransfer;

        // 3) gather adjacent storages (your “port == facing”)
        List<MIEnergyStorage> extractors = new ArrayList<>();
        List<MIEnergyStorage> receivers = new ArrayList<>();
        List<MIEnergyStorage> both = new ArrayList<>();
        Set<MIEnergyStorage> dedupe = Collections.newSetFromMap(new IdentityHashMap<>());

        for (var conn : connectors) {
            Direction port = conn.getFacing();
            BlockPos adj = conn.getBlockPos().relative(port);

            MIEnergyStorage st = level.getCapability(EnergyApi.SIDED, adj, port.getOpposite());
            if (st == null) continue;
            if (!st.canConnect(TIER)) continue;
            if (!dedupe.add(st)) continue;

            boolean ex = st.canExtract();
            boolean rc = st.canReceive();
            if (ex && rc) both.add(st);
            else if (ex) extractors.add(st);
            else if (rc) receivers.add(st);
        }

        if (level.getGameTime() % 20 == 0) {
            System.out.println("[MIW] conns=" + connectors.size()
                    + " net=" + networkAmount + "/" + networkCapacity
                    + " ex=" + extractors.size()
                    + " rx=" + receivers.size()
                    + " both=" + both.size());
        }

        // prevent pull+push to same “both” in one tick
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
                if (got > 0) used.addAll(tmp);
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

        // 5) rebalance across connector buffers
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