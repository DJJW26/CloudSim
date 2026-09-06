package org.cloudbus.cloudsim.examples.power.random;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 * Power-Aware Best-Fit Decreasing (PABFD) allocation policy.
 *
 * <p>Textbook PABFD (Beloglazov et al.): VMs are considered in decreasing
 * order of CPU demand (our VM list from Helper.createVmList is already
 * created largest-first, and Pabfd.java sorts defensively), and each VM is
 * placed on the host where the estimated power increase is minimal.
 *
 * <p>Unlike MBFD/ThrMmt there is NO migration or overload detection here:
 * optimizeAllocation() returns null so placement is static. That is exactly
 * the difference you want to show: PABFD = power-aware placement only,
 * MBFD = PABFD placement + dynamic consolidation via migration.
 */
public class PowerVmAllocationPolicyPabfd extends VmAllocationPolicy {

	private final Map<String, Host> vmTable = new HashMap<String, Host>();

	public PowerVmAllocationPolicyPabfd(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		PowerHost foundHost = findHostForVm(vm);
		if (foundHost == null) {
			Log.formatLine("%.2f: No suitable host found for VM #%d\n", org.cloudbus.cloudsim.core.CloudSim.clock(), vm.getId());
			return false;
		}
		return allocateHostForVm(vm, foundHost);
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) {
			vmTable.put(vm.getUid(), host);
			Log.formatLine(
					"%.2f: VM #%d has been allocated to the host #%d",
					org.cloudbus.cloudsim.core.CloudSim.clock(),
					vm.getId(),
					host.getId());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #%d on the host #%d failed\n",
				org.cloudbus.cloudsim.core.CloudSim.clock(),
				vm.getId(),
				host.getId());
		return false;
	}

	/**
	 * Best-fit by minimum power increase, skipping hosts that would be
	 * over-utilized (> 100%) after the placement.
	 */
	public PowerHost findHostForVm(Vm vm) {
		double minPowerDiff = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		double vmTotalMips = vm.getMips() * vm.getNumberOfPes();

		for (Host h : getHostList()) {
			PowerHost host = (PowerHost) h;
			if (!host.isSuitableForVm(vm)) {
				continue;
			}
			double totalMips = host.getTotalMips();
			double hostUtilMips = 0;
			try {
				hostUtilMips = ((org.cloudbus.cloudsim.HostDynamicWorkload) host).getUtilizationOfCpuMips();
			} catch (Exception e) {
				hostUtilMips = ((org.cloudbus.cloudsim.HostDynamicWorkload) host).getUtilizationOfCpu() * totalMips;
			}
			double afterUtil = (hostUtilMips + vmTotalMips) / totalMips;
			if (afterUtil > 1.0) {
				continue; // would overload the host
			}
			double powerAfter;
			try {
				powerAfter = host.getPowerModel().getPower(afterUtil);
			} catch (Exception e) {
				continue;
			}
			double powerDiff = powerAfter - host.getPower();
			if (powerDiff < minPowerDiff) {
				minPowerDiff = powerDiff;
				allocatedHost = host;
			}
		}
		return allocatedHost;
	}

	/** No dynamic consolidation: static placement only. */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = vmTable.remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return vmTable.get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return vmTable.get(Vm.getUid(userId, vmId));
	}
}
