package org.cloudbus.cloudsim.examples.power.random;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
 * PABFD experiment: Power-Aware Best-Fit Decreasing, static placement only.
 *
 * <p>Each VM is placed on the host with the minimum estimated power increase
 * (see PowerVmAllocationPolicyPabfd). Migrations are disabled, so unlike
 * ThrMmt (MBFD) there is no dynamic consolidation. Compare against
 * RoundRobin (power-unaware placement) and ThrMmt (placement + migration).
 */
public class Pabfd {

	public static void main(String[] args) throws IOException {
		String experimentName = "random_pabfd";
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

			// Best-Fit Decreasing: largest VMs first
			Collections.sort(vmList, new Comparator<Vm>() {
				@Override
				public int compare(Vm a, Vm b) {
					double ma = a.getMips() * a.getNumberOfPes();
					double mb = b.getMips() * b.getNumberOfPes();
					return Double.compare(mb, ma);
				}
			});

			List<PowerHost> hostList = Helper.createHostList(RandomConstants.NUMBER_OF_HOSTS);

			PowerDatacenter datacenter = (PowerDatacenter) Helper.createDatacenter(
					"Datacenter",
					PowerDatacenter.class,
					hostList,
					new PowerVmAllocationPolicyPabfd(hostList));

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
