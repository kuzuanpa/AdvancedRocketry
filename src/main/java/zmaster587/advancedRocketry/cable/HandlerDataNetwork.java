package zmaster587.advancedRocketry.cable;

import org.jetbrains.annotations.NotNull;

public class HandlerDataNetwork extends HandlerCableNetwork {
	@Override
	public int getNewNetworkID() {
		DataNetwork net = DataNetwork.initNetwork();

		networks.put(net.networkID, net);

		return net.networkID;
	}
	
	
	public int getNewNetworkID(int id) {
		@NotNull DataNetwork net = new DataNetwork();
		net.networkID = id;

		networks.put(net.networkID, net);

		return net.networkID;
	}


}
