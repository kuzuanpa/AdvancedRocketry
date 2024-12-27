package zmaster587.advancedRocketry.cable;

public class HandlerLiquidNetwork extends HandlerCableNetwork {
	
	@Override
	public int getNewNetworkID() {
		LiquidNetwork net = LiquidNetwork.initNetwork();

		networks.put(net.networkID, net);

		return net.networkID;
	}
}
