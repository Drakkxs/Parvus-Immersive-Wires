package com.tom.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.network.SimpleNetworkHandler;

public class SFMNetworkHandler extends SimpleNetworkHandler<SFMConnectorBlockEntity, SFMNetworkHandler> {
	protected SFMNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
		super(net, global);
	}

	@Override
	protected SFMConnectorBlockEntity connect(IImmersiveConnectable iic) {
		if(iic instanceof SFMConnectorBlockEntity te) return te;
		return null;
	}

	@Override
	protected void setNetworkHandler(SFMConnectorBlockEntity c, SFMNetworkHandler handler) {
		// SFM doesn't need network handler notification
		// Network updates are handled by the mixin
	}
}