package com.meshsim.util;

import java.util.Arrays;

/**
 * Uses java.util.Arrays deliberately (Unit 5 "Arrays utility class"):
 * sort/binarySearch/fill/copyOf for medians and percentiles over raw metric
 * samples (per-run PDR, hop counts, latency, etc).
 */
public final class MetricsAggregator {

    private double[] samples = new double[16];
    private int count = 0;

    public void record(double value) {
        if (count == samples.length) {
            samples = Arrays.copyOf(samples, samples.length * 2);
        }
        samples[count++] = value;
    }

    public double median() {
        if (count == 0) return 0.0;
        double[] sorted = Arrays.copyOf(samples, count);
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        return (sorted.length % 2 == 0) ? (sorted[mid - 1] + sorted[mid]) / 2.0 : sorted[mid];
    }

    public double percentile(double p) {
        if (count == 0) return 0.0;
        double[] sorted = Arrays.copyOf(samples, count);
        Arrays.sort(sorted);
        int index = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }

    public boolean contains(double value) {
        double[] sorted = Arrays.copyOf(samples, count);
        Arrays.sort(sorted);
        return Arrays.binarySearch(sorted, value) >= 0;
    }

    public void reset() {
        Arrays.fill(samples, 0.0);
        count = 0;
    }

    public int count() {
        return count;
    }
}
