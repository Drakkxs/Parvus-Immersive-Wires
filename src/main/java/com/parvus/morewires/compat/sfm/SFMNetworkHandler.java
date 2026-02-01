package com.parvus.morewires.compat.sfm;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.parvus.morewires.MoreImmersiveWires;
import com.parvus.morewires.network.NodeNetworkHandler;
import net.minecraft.world.level.Level;

public class SFMNetworkHandler extends NodeNetworkHandler<ConnectionWrapper, IGridNode> implements IGridNodeListener<com.parvus.morewires.compat.ae.AENetworkHandler> {

	protected SFMNetworkHandler(LocalWireNetwork net, GlobalWireNetwork global) {
		super(net, global);
	}

	@Override
	public void onSaveChanges(com.parvus.morewires.compat.sfm.SFMNetworkHandler var1, IGridNode var2) {

	}

	@Override
	protected void clearConnection(ConnectionWrapper c) {
		if(c.getConnection() != null)c.getConnection().destroy();
	}

	@Override
	protected void initNode(Level level) {
		mainNode.create(level, null);
	}

	@Override
	protected IGridNode getNode() {
		return mainNode.getNode();
	}

	@Override
	protected ConnectionWrapper connect(IImmersiveConnectable iic, IGridNode nodeIn) {
		if(iic instanceof AENetworkedBlockEntity te) {
			IGridNode node = te.getActionableNode();
			if(node != null) {
				return new ConnectionWrapper(GridHelper.createConnection(nodeIn, node));
			}
			needRefresh = true;
		}
		return null;
	}

	@Override
	protected void connectFirst(IImmersiveConnectable iic, IGridNode main) {
		if(iic instanceof AENetworkedBlockEntity te) {
			IGridNode node = te.getActionableNode();
			if(node != null) {
				mainNode.setOwningPlayerId(node.getOwningPlayerId());
			}
		}
	}
}