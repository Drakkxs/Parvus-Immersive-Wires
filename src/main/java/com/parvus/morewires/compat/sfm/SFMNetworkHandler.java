package com.parvus.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.parvus.morewires.network.SimpleNetworkHandler;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;

public class SFMNetworkHandler extends SimpleNetworkHandler<SFMConnectorBlockEntity, SFMNetworkHandler> {

	protected SFMNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
		super(net, global);
	}

	@Override
	protected SFMConnectorBlockEntity connect(IImmersiveConnectable iic) {
		if(iic instanceof SFMConnectorBlockEntity te)return te;
		else return null;
	}

	@Override
	protected void setNetworkHandler(SFMConnectorBlockEntity c, SFMNetworkHandler handler) {
		c.networkChanged();
	}

	public void addConnections(ConnectionSink sink) {
		visitAll(s -> sink.tryConnect(s.getGlobalPos(), null));
	}
}
