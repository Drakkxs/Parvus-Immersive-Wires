package com.tom.morewires.compat.mi.util;

import aztech.modern_industrialization.api.energy.EnergyApi;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.compat.mi.MILvConnectorBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MoreImmersiveWires.modid, bus = EventBusSubscriber.Bus.MOD)
public final class MICapabilities {
    private MICapabilities() {}

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        @SuppressWarnings("unchecked")
        BlockEntityType<MILvConnectorBlockEntity> type =
                (BlockEntityType<MILvConnectorBlockEntity>) MoreImmersiveWires.MI_LV_WIRE.simple().CONNECTOR_ENTITY.get();

        event.registerBlockEntity(EnergyApi.SIDED, type, (be, side) -> {
            Direction port = be.getFacing().getOpposite();
            if (side == null || side == port) return be.getExposedEnergy();
            return null;
        });
    }
}