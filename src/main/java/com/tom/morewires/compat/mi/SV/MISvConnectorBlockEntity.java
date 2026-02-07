package com.tom.morewires.compat.mi.SV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MISvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MISvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_SV_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.SUPERCONDUCTOR,
                MoreImmersiveWires.MI_SV_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_SV_WIRE.simple().wireType
        );
    }
}