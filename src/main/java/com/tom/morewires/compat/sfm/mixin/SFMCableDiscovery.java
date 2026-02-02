package com.tom.morewires.compat.sfm.mixin;

import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.util.SFMStreamUtils;
import com.tom.morewires.compat.sfm.util.SFMIEAdjacency;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(CableNetwork.class)
public abstract class SFMCableDiscovery {
    @Inject(method = "discoverCables", at = @At("HEAD"), cancellable = true)
    private static void onDiscoverCables(Level level, BlockPos start, CallbackInfoReturnable<Stream<BlockPos>> cir) {
        if (level == null || !CableNetwork.isCable(level, start)) return;

        Stream<BlockPos> stream = SFMStreamUtils.<BlockPos, BlockPos>getRecursiveStream(
                (current, next, results) -> {
                    results.accept(current);

                    SFMIEAdjacency.forEachCableNeighbor(level, current, neighbor -> {
                        if (CableNetwork.isCable(level, neighbor)) {
                            next.accept(neighbor.immutable());
                        }
                    });
                },
                start
        );

        cir.setReturnValue(stream);
    }
}
