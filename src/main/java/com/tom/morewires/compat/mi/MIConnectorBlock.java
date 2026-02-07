package com.tom.morewires.compat.mi;

import com.tom.morewires.block.OnCableConnectorBlock;
import com.tom.morewires.compat.mi.util.MIPipeHook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiPredicate;

public class MIConnectorBlock<T extends MIConnectorBlockEntityBase> extends OnCableConnectorBlock<T> {

    public MIConnectorBlock(
            DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> type,
            BiPredicate<BlockGetter, BlockPos> isOnCable
    ) {
        super(type, isOnCable);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide) return;
        if (oldState.getBlock() == state.getBlock()) return;

        var be = level.getBlockEntity(pos);
        if (be instanceof MIConnectorBlockEntityBase conn) {
            MIPipeHook.rescanAdjacentPipes(level, pos, conn.getCableTier());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            var be = level.getBlockEntity(pos);
            if (be instanceof MIConnectorBlockEntityBase) {
                MIPipeHook.removeAdjacentElectricConnections(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
