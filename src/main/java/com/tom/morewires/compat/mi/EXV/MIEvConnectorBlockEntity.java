package com.tom.morewires.compat.mi.EXV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MIEvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MIEvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_EV_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.EV,
                MoreImmersiveWires.MI_EV_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_EV_WIRE.simple().wireType
        );
    }
}