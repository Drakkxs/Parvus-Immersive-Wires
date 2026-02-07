package com.tom.morewires.compat.mi.SCV;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.compat.mi.MINetworkHandler;

public class MISCvNetworkHandler extends MINetworkHandler<MISCvConnectorBlockEntity> {
    protected MISCvNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected MISCvConnectorBlockEntity connect(IImmersiveConnectable iic) {
        return iic instanceof MISCvConnectorBlockEntity be ? be : null;
    }
}