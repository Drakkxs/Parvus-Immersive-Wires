package com.parvus.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.cablenetwork.ICableBlock;
import com.parvus.morewires.network.NodeNetworkHandler;

public class SFMNetworkHandler extends NodeNetworkHandler<CableNetwork, CableNetworkManager>  {

	protected SFMNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
		super(net, global);
	}

	@Override
	protected void clearConnection(CableNetwork cableNetwork) {
		CableNetworkManager.unregisterNetworkForTestingPurposes(cableNetwork);
	}

	@Override
	protected CableNetworkManager getNode() {
		return null;
	}

	@Override
	protected CableNetwork connect(IImmersiveConnectable iic, CableNetworkManager node) {
		return null;
	}

	@Override
	protected void connectFirst(IImmersiveConnectable iic, CableNetworkManager main) {

	}


}