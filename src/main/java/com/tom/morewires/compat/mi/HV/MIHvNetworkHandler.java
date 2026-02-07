package com.tom.morewires.compat.mi.HV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MIHvNetworkHandler extends MINetworkHandler<MIHvConnectorBlockEntity> {
    protected MIHvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MIHvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MIHvConnectorBlockEntity be ? be : null;
    }
}