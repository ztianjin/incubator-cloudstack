// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cloud.utils.concurrency.NamedThreadFactory;

public class MetricsCollector {
    private static final Logger s_logger = Logger.getLogger(MetricsCollector.class);

	private final Set<String> vmNames = new HashSet<String>();
	private final Set<String> newVMnames = new HashSet<String>();
	private final Map<String, MockVmMetrics> metricsMap = new HashMap<String, MockVmMetrics>();

	private final transient ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Metrics"));

	private Set<String> _currentVms;

	public MetricsCollector(Set<String> currentVms) {
		_currentVms = currentVms;
		getAllVMNames();
	}

	public MetricsCollector() {

	}

	public synchronized void getAllVMNames() {
		Set<String> currentVMs = _currentVms;

        newVMnames.clear();
        newVMnames.addAll(currentVMs);
        newVMnames.removeAll(vmNames); //leave only new vms

        vmNames.removeAll(currentVMs); //old vms - current vms --> leave non-running vms;
        for (String vm: vmNames) {
		removeVM(vm);
        }

        vmNames.clear();
        vmNames.addAll(currentVMs);
	}

	public synchronized void submitMetricsJobs() {
		s_logger.debug("Submit Metric Jobs called");

		for (String vm : newVMnames) {
			MockVmMetrics task = new MockVmMetrics(vm);
			if (!metricsMap.containsKey(vm)) {
			    metricsMap.put(vm, task);
			    ScheduledFuture<?> sf = executor.scheduleWithFixedDelay(task, 2, 600, TimeUnit.SECONDS);
			    task.setFuture(sf);
			}
		}
		newVMnames.clear();
	}

	public synchronized void addVM(String vmName) {
		newVMnames.add(vmName);
		s_logger.debug("Added vm name= " + vmName);
	}

	public synchronized void removeVM(String vmName) {
		newVMnames.remove(vmName);
		vmNames.remove(vmName);
		MockVmMetrics task = metricsMap.get(vmName);
	if (task != null) {
		task.stop();
		boolean r1= task.getFuture().cancel(false);
		metricsMap.remove(vmName);
		s_logger.debug("removeVM: cancel returned " + r1 + " for VM " + vmName);
	} else {
		s_logger.warn("removeVM called for nonexistent VM " + vmName);
	}
	}

	public synchronized Set<String> getVMNames() {
	  return vmNames;
	}

	public synchronized Map<String, MockVmMetrics> getMetricsMap() {
		return metricsMap;
	}
}
