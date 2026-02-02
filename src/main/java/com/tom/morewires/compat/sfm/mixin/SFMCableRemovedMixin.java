package com.tom.morewires.compat.sfm.mixin;

import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(CableNetworkManager.class)
public abstract class SFMCableRemovedMixin {

    @Shadow @Final
    private static Map<Level, Long2ObjectMap<CableNetwork>> NETWORKS_BY_CABLE_POSITION;

    @Shadow @Final
    private static Map<Level, List<CableNetwork>> NETWORKS_BY_LEVEL;

    @Shadow
    private static void removeNetwork(CableNetwork network) {}

    @Shadow
    private static void addNetwork(CableNetwork network) {}

    @Inject(method = "onCableRemoved", at = @At("HEAD"), cancellable = true)
    private static void morewires_onCableRemoved(Level level, BlockPos cablePos, CallbackInfo ci) {
        if (level == null || level.isClientSide()) return;

        // Find cached network for this position
        Long2ObjectMap<CableNetwork> posMap = NETWORKS_BY_CABLE_POSITION.get(level);
        if (posMap == null) return;

        CableNetwork old = posMap.get(cablePos.asLong());
        if (old == null) return;

        // Invalidate/remove the old network (like vanilla)
        removeNetwork(old);

        // Keep SFM's "only rebuild small networks" optimization
        if (old.getCableCount() > 256) {
            ci.cancel();
            return;
        }

        // Remaining candidates = all old positions that are still "cables" in-world
        LongOpenHashSet remaining = new LongOpenHashSet();
        old.getCablePositionsRaw().forEach((long p) -> {
            BlockPos bp = BlockPos.of(p);
            if (CableNetwork.isCable(level, bp)) {
                remaining.add(p);
            }
        });

        // Rebuild connected components from world
        while (!remaining.isEmpty()) {
            long seedLong = remaining.iterator().nextLong();
            remaining.remove(seedLong);

            BlockPos seed = BlockPos.of(seedLong);
            if (!CableNetwork.isCable(level, seed)) continue;

            CableNetwork rebuilt = new CableNetwork(level);
            rebuilt.rebuildNetwork(seed); // <-- uses your wire-aware discoverCables now
            addNetwork(rebuilt);

            // Remove all positions belonging to this rebuilt network from the remaining set
            rebuilt.getCablePositionsRaw().forEach(remaining::remove);
        }

        ci.cancel();
    }
}
