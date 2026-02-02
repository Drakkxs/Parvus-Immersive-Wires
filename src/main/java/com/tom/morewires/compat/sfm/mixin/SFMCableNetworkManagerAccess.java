package com.tom.morewires.compat.sfm.mixin;

import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(CableNetworkManager.class)
public interface SFMCableNetworkManagerAccess {
    @Accessor("NETWORKS_BY_CABLE_POSITION")
    static Map<Level, Long2ObjectMap<CableNetwork>> morewires$getPosMap() {
        throw new AssertionError();
    }

    @Accessor("NETWORKS_BY_LEVEL")
    static Map<Level, List<CableNetwork>> morewires$getLevelMap() {
        throw new AssertionError();
    }

    @Invoker("removeNetwork")
    static void morewires$removeNetwork(CableNetwork network) {
        throw new AssertionError();
    }

    @Invoker("addNetwork")
    static void morewires$addNetwork(CableNetwork network) {
        throw new AssertionError();
    }
}