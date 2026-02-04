package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.network.NodeNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class MILvNetworkHandler extends NodeNetworkHandler<BlockPos, BlockPos> {
    private static final CableTier TIER = CableTier.LV;

    // Our own mirror of “who’s in this local wire network”
    private final Set<BlockPos> members = new HashSet<>();
    private BlockPos mainPos;

    protected MILvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    // Called when base resets (merge, connector load/unload, wire add/remove)
    @Override
    protected void clearConnection(BlockPos pos) {
        members.remove(pos);
        if (Objects.equals(mainPos, pos)) mainPos = null;
    }

    @Override
    protected BlockPos getNode() {
        return mainPos;
    }

    // Called once per rebuild for the first connector encountered
    @Override
    protected void connectFirst(IImmersiveConnectable iic, BlockPos ignoredMain) {
        // iic is the connector in the wire network
        mainPos = iic.getPosition();
        members.add(mainPos);
    }

    // Called for every connector in rebuild pass
    @Override
    protected BlockPos connect(IImmersiveConnectable iic, BlockPos main) {
        BlockPos pos = iic.getPosition();
        members.add(pos);
        return pos;
    }

    @Override
    public void update(Level level) {
        super.update(level); // IMPORTANT: keeps members in sync via the callbacks above

        if (!(level instanceof ServerLevel server)) return;
        if (members.isEmpty()) return;

        tickNetwork(server);
    }

    private void tickNetwork(ServerLevel level) {
        final long maxTransfer = TIER.getMaxTransfer();

        // 1) Resolve positions -> connector BEs that are actually loaded right now
        List<MILvConnectorBlockEntity> connectors = new ArrayList<>(members.size());
        for (BlockPos pos : members) {
            var be = level.getBlockEntity(pos);
            if (be instanceof MILvConnectorBlockEntity conn && !conn.isRemoved()) {
                connectors.add(conn);
            }
        }
        if (connectors.isEmpty()) return;

        // 2) Sum storage across loaded connectors
        long networkAmount = 0;
        for (var c : connectors) networkAmount += c.getNodeEu();

        long networkCapacity = (long) connectors.size() * maxTransfer;

        // 3) Gather adjacent MI storages only from the connector “port” side
        List<MIEnergyStorage> storages = new ArrayList<>();
        Set<MIEnergyStorage> dedupe = Collections.newSetFromMap(new IdentityHashMap<>());

        for (var conn : connectors) {
            Direction port = conn.getFacing().getOpposite();
            BlockPos adj = conn.getBlockPos().relative(port);

            MIEnergyStorage st = level.getCapability(EnergyApi.SIDED, adj, port.getOpposite());
            if (st == null) continue;
            if (!st.canConnect(TIER)) continue;
            if (st == conn.getExposedEnergy()) continue; // avoid self

            if (dedupe.add(st)) storages.add(st);
        }

        // 4) MI-style transfer (tier-limited, fair)
        if (!storages.isEmpty()) {
            long extractMax = Math.min(maxTransfer, networkCapacity - networkAmount);
            if (extractMax > 0) networkAmount += transferForTargets(MIEnergyStorage::extract, storages, extractMax);

            long insertMax = Math.min(maxTransfer, networkAmount);
            if (insertMax > 0) networkAmount -= transferForTargets(MIEnergyStorage::receive, storages, insertMax);
        }

        // 5) Rebalance evenly across nodes (MI behavior)
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
        for (int i = 0; i < targets.size(); i++) {
            sim[i] = op.transfer(targets.get(i), maxAmount, true);
        }

        Integer[] idx = new Integer[targets.size()];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingLong(i -> sim[i]));

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