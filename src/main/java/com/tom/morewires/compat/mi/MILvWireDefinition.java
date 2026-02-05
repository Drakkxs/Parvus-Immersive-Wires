package com.tom.morewires.compat.mi;

import aztech.modern_industrialization.api.energy.EnergyApi;
import blusunrize.immersiveengineering.api.wires.localhandlers.ILocalHandlerConstructor;
import com.tom.morewires.SimpleWireTypeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MILvWireDefinition extends SimpleWireTypeDefinition<MILvConnectorBlockEntity> {
    private static final Logger log = LoggerFactory.getLogger(MILvWireDefinition.class);

    public MILvWireDefinition() {
        super("mi_lv", "MI Low Voltage", 0x17161f);
    }

    @Override
    protected ILocalHandlerConstructor createLocalHandler() {
        return MILvNetworkHandler::new; // or noop if you're not using IE energy logic
    }

    @Override
    public Block makeBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<MILvConnectorBlockEntity>> type) {
        return new MILvConnectorBlock(type, this::isCable);
    }

    @Override
    public MILvConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        return new MILvConnectorBlockEntity(pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof MILvConnectorBlock;
    }

    @Override
    public void registerCapabilities(RegisterCapabilitiesEvent event) {

        // 1) MI native
        event.registerBlockEntity(EnergyApi.SIDED, CONNECTOR_ENTITY.get(), (be, side) -> {
            // MI often probes with null
            if (side == null) return be.getExposedEnergy();

            Direction port = be.getFacing(); // your “port == facing” discovery
            return side == port ? be.getExposedEnergy() : null;
        });
    }
}