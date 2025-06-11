package org.example.GA;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class CPUTimer {
    private long startCpuTime;
    private final OperatingSystemMXBean osBean;

    public CPUTimer() {
        this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    }

    public void start() {
        this.startCpuTime = osBean.getProcessCpuTime();
    }

    public double getTotalCPUTimeSeconds() {
        long endCpuTime = osBean.getProcessCpuTime();
        return (endCpuTime - startCpuTime) / 1_000_000_000.0;
    }
}

