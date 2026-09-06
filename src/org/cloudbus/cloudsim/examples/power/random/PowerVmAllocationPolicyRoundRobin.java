package org.cloudbus.cloudsim.examples.power.random;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * Round-Robin allocation policy (power-unaware baseline).
 *
 * <p>Cycles through hosts in order (host0, host1, ..., hostN, host0, ...)
 * and places each VM on the next suitable host. Ignores power consumption
 * entirely. No migration: optimizeAllocation() returns null.
 *
 * <p>Contrast with PABFD (places by minimum power increase) and
 * MBFD/ThrMmt (PABFD placement + migration-based consolidation).
 */
public class PowerVmAllocationPolicyRoundRobin extends VmAllocationPolicy {

	private final Map<String, Host> vmTable = new HashMap<String, Host>();
	private int lastHostIndex = -1;

	public PowerVmAllocationPolicyRoundRobin(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host foundHost = findHostForVm(vm);
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

	/** Next suitable host in cyclic order. */
	public Host findHostForVm(Vm vm) {
		List<Host> hostList = getHostList();
		int n = hostList.size();
		for (int i = 1; i <= n; i++) {
			int idx = (lastHostIndex + i) % n;
			Host host = hostList.get(idx);
			if (host.isSuitableForVm(vm)) {
				lastHostIndex = idx;
				return host;
			}
		}
		return null;
	}

	/** No migration. */
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
