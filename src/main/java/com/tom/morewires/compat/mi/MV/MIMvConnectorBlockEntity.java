package com.tom.morewires.compat.mi.MV;

import aztech.modern_industrialization.api.energy.CableTier;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MIConnectorBlockEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MIMvConnectorBlockEntity extends MIConnectorBlockEntityBase {
    public MIMvConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(
                MoreImmersiveWires.MI_MV_WIRE.simple().CONNECTOR_ENTITY.get(),
                pos, state,
                CableTier.MV,
                MoreImmersiveWires.MI_MV_WIRE.simple().NET_ID,
                MoreImmersiveWires.MI_MV_WIRE.simple().wireType
        );
    }
}