package com.tom.morewires.compat.mi.SCV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MISCvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MISCvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_SUPERCONDUCTOR_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.SUPERCONDUCTOR,
                MoreImmersiveWires.MI_SUPERCONDUCTOR_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_SUPERCONDUCTOR_WIRE.simple().wireType
        );
    }
}