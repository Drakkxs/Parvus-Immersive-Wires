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

public abstract class MIWireDefinitionBase<T extends MIConnectorBlockEntityBase>
        extends SimpleWireTypeDefinition<T> {

    protected MIWireDefinitionBase(String name, String displayName, int color) {
        super(name, displayName, color);
    }

    /** Tier-specific network handler constructor */
    protected abstract ILocalHandlerConstructor createMIHandler();

    /** Tier-specific block factory (usually new MxConnectorBlock(type, this::isCable)) */
    protected abstract Block createMIBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> type);

    /** Tier-specific BE factory */
    protected abstract T createMIBE(BlockPos pos, BlockState state);

    /** Tier-specific block class for isCable() checks */
    protected abstract Class<? extends Block> getCableBlockClass();

    @Override
    protected final ILocalHandlerConstructor createLocalHandler() {
        return createMIHandler();
    }

    @Override
    public final Block makeBlock(DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> type) {
        return createMIBlock(type);
    }

    @Override
    public final T createBE(BlockPos pos, BlockState state) {
        return createMIBE(pos, state);
    }

    @Override
    public final boolean isCable(BlockGetter level, BlockPos pos) {
        return getCableBlockClass().isInstance(level.getBlockState(pos).getBlock());
    }

    @Override
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EnergyApi.SIDED, CONNECTOR_ENTITY.get(), (be, side) -> {
            // MI often probes with null
            if (side == null) return be.getExposedEnergy();

            // “port == facing” rule
            Direction port = be.getFacing();
            return side == port ? be.getExposedEnergy() : null;
        });
    }
}