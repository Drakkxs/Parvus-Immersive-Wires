package com.tom.morewires.compat.mi.HV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MIHvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MIHvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_HV_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.HV,
                MoreImmersiveWires.MI_HV_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_HV_WIRE.simple().wireType
        );
    }
}