package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.util.SFMDirections;
import ca.teamdman.sfm.common.util.SFMStreamUtils;
import com.tom.morewires.MoreImmersiveWires;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(CableNetwork.class)
public class SFMCableDiscoveryMixin {
    @Inject(method = "discoverCables", at = @At("HEAD"), cancellable = true)
    private static void discoverCables(Level level, BlockPos startPos, CallbackInfoReturnable<Stream<BlockPos>> cir) {
        Stream<BlockPos> original = SFMStreamUtils.getRecursiveStream(
                (current, next, results) -> {
                    results.accept(current);
                    // Add adjacent cables  
                    for (Direction d : SFMDirections.DIRECTIONS_WITHOUT_NULL) {
                        BlockPos target = current.relative(d);
                        if (CableNetwork.isCable(level, target)) {
                            next.accept(target);
                        }
                    }
                    // Add IE wire-connected endpoints  
                    if (level.getBlockEntity(current) instanceof SFMConnectorBlockEntity) {
                        addWireConnectedEndpoints(level, current, next);
                    }
                }, startPos
        );
        cir.setReturnValue(original);
    }

    private static void addWireConnectedEndpoints(Level level, BlockPos pos, Consumer<BlockPos> next) {
        GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(level);
        LocalWireNetwork localNet = globalNet.getNullableLocalNet(new ConnectionPoint(pos, 0));
        if (localNet != null) {
            for (Connection conn : localNet.getConnections(pos)) {
                if (conn.type == MoreImmersiveWires.SFM_WIRE.simple().wireType) {
                    BlockPos otherPos = conn.getOtherEnd(new ConnectionPoint(pos, 0)).position();
                    if (level.getBlockEntity(otherPos) instanceof SFMConnectorBlockEntity) {
                        next.accept(otherPos);
                    }
                }
            }
        }
    }
}