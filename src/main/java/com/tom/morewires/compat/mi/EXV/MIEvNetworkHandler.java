package com.tom.morewires.compat.mi.EXV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MIEvNetworkHandler extends MINetworkHandler<MIEvConnectorBlockEntity> {
    protected MIEvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MIEvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MIEvConnectorBlockEntity be ? be : null;
    }
}