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

public class MILvConnectorBlock extends OnCableConnectorBlock<MILvConnectorBlockEntity> {

    public MILvConnectorBlock(
            DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type,
            BiPredicate<BlockGetter, BlockPos> isOnCable
    ) {
        super(type, isOnCable);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide) return;
        if (oldState.getBlock() == state.getBlock()) return; // ignore pure state updates

        var be = level.getBlockEntity(pos);
        if (be instanceof MILvConnectorBlockEntity conn) {
            MIPipeHook.rescanAdjacentPipes(level, pos, conn.getCableTier());
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            // remove connections before the BE is gone
            var be = level.getBlockEntity(pos);
            if (be instanceof MILvConnectorBlockEntity conn) {
                MIPipeHook.removeAdjacentElectricConnections(level, pos);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}