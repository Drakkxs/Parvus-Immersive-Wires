package com.tom.morewires.compat.mi.MV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MIMvNetworkHandler extends MINetworkHandler<MIMvConnectorBlockEntity> {
    protected MIMvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MIMvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MIMvConnectorBlockEntity be ? be : null;
    }
}