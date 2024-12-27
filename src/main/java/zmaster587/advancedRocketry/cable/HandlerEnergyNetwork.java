package zmaster587.advancedRocketry.cable;

public class HandlerEnergyNetwork extends HandlerCableNetwork {
	@Override
	public int getNewNetworkID() {
		EnergyNetwork net = EnergyNetwork.initNetwork();

		networks.put(net.networkID, net);

		return net.networkID;
	}
}
