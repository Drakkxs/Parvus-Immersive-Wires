package com.tom.morewires.compat.sfm.util;

import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.sfm.mixin.SFMCableNetworkManagerAccess;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayDeque;
import java.util.Map;

@EventBusSubscriber(modid = MoreImmersiveWires.modid, bus = EventBusSubscriber.Bus.GAME) // Or your modid; either is fine for the event bus
public final class SFMTraversalScheduler {
    private SFMTraversalScheduler() {}

    // One job per level
    private static final Map<Level, Job> JOBS = new java.util.WeakHashMap<>();

    // Tuneables
    private static final int DEBOUNCE_TICKS = 5;        // coalesce spam
    private static final int NODES_PER_TICK = 64;       // budget
    private static final int MAX_VISITED = 200_000;     // safety; prevents infinite growth

    /** Mark a level/position dirty. Cancels and restarts traversal (cheap). */
    public static void markDirty(Level level, BlockPos seed) {
        if (!(level instanceof ServerLevel)) return;

        Job job = JOBS.computeIfAbsent(level, l -> new Job());
        job.generation++;
        job.lastChangeTick = level.getGameTime();
        job.seed = seed.immutable();

        // Cancel current work immediately
        job.queue.clear();
        job.visited.clear();
        job.baseNet = null;
        job.started = false;

        System.out.println("[MIW:SFM] markDirty " + level.dimension().location() + " @ " + seed);
    }

    private static boolean printed = false;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) return;

        Job job = JOBS.get(level);
        if (job == null || job.seed == null) return;

        long now = level.getGameTime();
        if (now - job.lastChangeTick < DEBOUNCE_TICKS) return;

        // Start job if needed
        if (!job.started) {
            job.started = true;
            job.jobGeneration = job.generation;

            // Must be traversable to start, otherwise nothing to do.
            if (!SFMIEAdjacency.isTraversable(level, job.seed)) {
                job.seed = null;
                job.started = false;
                return;
            }

            job.queue.add(job.seed);
            job.visited.add(job.seed.asLong());

            // Choose / reuse a base network if one already exists nearby
            job.baseNet = findAnyExistingNetworkAt(level, job.seed);
            if (job.baseNet == null) {
                job.baseNet = new CableNetwork(level);
                // Register it immediately so future placements can find it
                SFMCableNetworkManagerAccess.morewires$addNetwork(job.baseNet);
            }

            // Clear capability cache once per rebuild
            job.baseNet.getLevelCapabilityCache().clear();
        }

        // Cancel if changed mid-run
        if (job.jobGeneration != job.generation) return;

        int budget = NODES_PER_TICK;
        while (budget-- > 0 && !job.queue.isEmpty()) {
            // Cancel if changed mid-run
            if (job.jobGeneration != job.generation) return;

            if (job.visited.size() > MAX_VISITED) {
                // Hard safety break: stop the job rather than killing TPS forever.
                job.seed = null;
                job.queue.clear();
                job.visited.clear();
                job.started = false;
                return;
            }

            BlockPos cur = job.queue.removeFirst();

            // If this is a real SFM cable, add it to base network and map it
            if (CableNetwork.isCable(level, cur)) {
                incorporateCableIntoBase(level, job, cur);
            }

            // Traverse neighbors (including IE relays/connectors)
            SFMIEAdjacency.forEachNetworkNeighbor(level, cur, n -> {
                if (!SFMIEAdjacency.isTraversable(level, n)) return;
                long key = n.asLong();
                if (job.visited.add(key)) {
                    job.queue.add(n.immutable());
                }
            });
        }

        // Finished?
        if (job.queue.isEmpty()) {
            job.seed = null;
            job.visited.clear();
            job.started = false;
            job.baseNet = null;
        }
    }

    /** Add a cable to baseNet; if it belongs to another net, merge that net into baseNet. */
    private static void incorporateCableIntoBase(Level level, Job job, BlockPos cablePos) {
        CableNetwork base = job.baseNet;
        if (base == null) return;

        Map<Level, Long2ObjectMap<CableNetwork>> allPosMaps = SFMCableNetworkManagerAccess.morewires$getPosMap();
        Long2ObjectMap<CableNetwork> posMap =
                allPosMaps.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>());

        CableNetwork existing = posMap.get(cablePos.asLong());
        if (existing != null && existing != base) {
            // Merge existing into base, and remove existing from lookups
            base.mergeNetwork(existing);
            SFMCableNetworkManagerAccess.morewires$removeNetwork(existing);

            // Rewrite mapping of all cables in existing to base
            existing.getCablePositionsRaw().forEach((long p) -> posMap.put(p, base));
        }

        // Ensure membership + mapping for this cable
        if (!base.containsCablePosition(cablePos)) base.addCable(cablePos);
        posMap.put(cablePos.asLong(), base);
    }

    /** Find an existing net at the seed if it’s already a cable, else none. */
    private static CableNetwork findAnyExistingNetworkAt(Level level, BlockPos seed) {
        if (!CableNetwork.isCable(level, seed)) return null;

        Map<Level, Long2ObjectMap<CableNetwork>> allPosMaps = SFMCableNetworkManagerAccess.morewires$getPosMap();
        Long2ObjectMap<CableNetwork> posMap = allPosMaps.get(level);
        if (posMap == null) return null;

        return posMap.get(seed.asLong());
    }

    private static final class Job {
        BlockPos seed;
        boolean started;

        long lastChangeTick;
        int generation;
        int jobGeneration;

        final ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        final LongOpenHashSet visited = new LongOpenHashSet();

        CableNetwork baseNet;
    }
}