package com.tom.morewires.compat.mi.LV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MILvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MILvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_LV_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.LV,
                MoreImmersiveWires.MI_LV_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_LV_WIRE.simple().wireType
        );
    }
}