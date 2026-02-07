package com.tom.morewires.compat.mi.SV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MISvNetworkHandler extends MINetworkHandler<MISvConnectorBlockEntity> {
    protected MISvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MISvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MISvConnectorBlockEntity be ? be : null;
    }
}