package com.tom.morewires.compat.sfm.mixin;

import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.sfm.util.SFMIEAdjacency;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(CableNetworkManager.class)
public abstract class SFMAfterGetOrRegister {
    @Shadow @Final
    private static Map<Level, Long2ObjectMap<CableNetwork>> NETWORKS_BY_CABLE_POSITION;

    @Shadow @Final
    private static Map<Level, List<CableNetwork>> NETWORKS_BY_LEVEL;

    @Inject(method = "getOrRegisterNetworkFromCablePosition", at = @At("RETURN"))
    private static void afterGetOrRegister(
            Level level,
            BlockPos pos,
            CallbackInfoReturnable<Optional<CableNetwork>> cir
    ) {
        Optional<CableNetwork> opt = cir.getReturnValue();
        if (level == null || level.isClientSide() || opt.isEmpty()) return;

        CableNetwork net = opt.get();

        // Global lookup map for this level
        Long2ObjectMap<CableNetwork> posMap =
                NETWORKS_BY_CABLE_POSITION.computeIfAbsent(level, k -> new Long2ObjectOpenHashMap<>());

        // BFS over cable positions, using physical + wire adjacency.
        ArrayDeque<BlockPos> q = new ArrayDeque<>();
        LongOpenHashSet seen = new LongOpenHashSet();

        // Only start if the position is actually a cable
        if (!CableNetwork.isCable(level, pos)) return;

        q.add(pos);
        seen.add(pos.asLong());

        while (!q.isEmpty()) {
            BlockPos cur = q.removeFirst();

            // This should already be true due to enqueue filter, but keep it safe.
            if (!CableNetwork.isCable(level, cur)) continue;

            // If this position already belongs to a different cached network, merge it in.
            CableNetwork existing = posMap.get(cur.asLong());
            if (existing != null && existing != net) {
                // Merge existing into net
                net.mergeNetwork(existing);

                // Remove existing from per-level list (avoid stale networks lingering)
                List<CableNetwork> networksForLevel = NETWORKS_BY_LEVEL.get(level);
                if (networksForLevel != null) {
                    networksForLevel.remove(existing);
                }

                // Rewrite all existing network cable positions to point to net
                existing.getCablePositionsRaw().forEach(cablePosLong -> posMap.put(cablePosLong, net));
            }

            // Ensure membership + lookup
            if (!net.containsCablePosition(cur)) {
                net.addCable(cur);
            }
            posMap.put(cur.asLong(), net);

            // Enqueue only valid cable neighbors (keeps BFS tight)
            SFMIEAdjacency.forEachNetworkNeighbor(level, cur, neighbor -> {
                if (!SFMIEAdjacency.isTraversable(level, neighbor)) return;
                long key = neighbor.asLong();
                if (seen.add(key)) q.add(neighbor.immutable());
            });


        }

        net.getLevelCapabilityCache().clear();

    }
}
