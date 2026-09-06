package org.cloudbus.cloudsim.examples.power.random;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 * Round-Robin experiment: power-unaware cyclic placement, no migration.
 *
 * <p>VMs are placed host0, host1, ..., hostN, host0, ... regardless of power.
 * Migrations disabled. Compare against Pabfd (power-aware placement) and
 * ThrMmt/MBFD (placement + migration consolidation).
 */
public class RoundRobin {

	public static void main(String[] args) throws IOException {
		String experimentName = "random_roundrobin";
		String outputFolder = "output";

		Log.setDisabled(!Constants.ENABLE_OUTPUT);
		Log.printLine("Starting " + experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			DatacenterBroker broker = Helper.createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = RandomHelper.createCloudletList(
					brokerId,
					RandomConstants.NUMBER_OF_VMS);
			List<Vm> vmList = Helper.createVmList(brokerId, cloudletList.size());
			List<PowerHost> hostList = Helper.createHostList(RandomConstants.NUMBER_OF_HOSTS);

			PowerDatacenter datacenter = (PowerDatacenter) Helper.createDatacenter(
					"Datacenter",
					PowerDatacenter.class,
					hostList,
					new PowerVmAllocationPolicyRoundRobin(hostList));

			datacenter.setDisableMigrations(true);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			Helper.printResults(
					datacenter,
					vmList,
					lastClock,
					experimentName,
					Constants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}

		Log.printLine("Finished " + experimentName);
	}
}
