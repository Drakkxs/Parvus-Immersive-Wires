package com.tom.morewires.compat.mi.EXV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MIEXvNetworkHandler extends MINetworkHandler<MIEXvConnectorBlockEntity> {
    protected MIEXvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MIEXvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MIEXvConnectorBlockEntity be ? be : null;
    }
}