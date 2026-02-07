package com.tom.morewires.compat.mi.LV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MILvNetworkHandler extends MINetworkHandler<MILvConnectorBlockEntity> {
    protected MILvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MILvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MILvConnectorBlockEntity be ? be : null;
    }
}