package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class RunTimesTable {

	private List<String> datasets;

	private String name;

	private List<String> phases;

	private int runs;

	// phase, run, data set, time
	private Map<String, Map<Integer, Map<String, Long>>> table;

	public RunTimesTable(String name) {
		this.name = name;
		this.datasets = new LinkedList<String>();
		this.phases = new LinkedList<String>();
		this.table = new HashMap<String, Map<Integer, Map<String, Long>>>();
		this.runs = 1;
	}

	private long average(String phase, String dataset) {
		Map<Integer, Map<String, Long>> phaseMap = table.get(phase);
		if (phaseMap == null)
			return -1;
		long ac = 0;
		for (int run = 1; run <= runs; run++) {
			long time = get(phase, run, dataset);
			if (time == -1)
				return -1;
			ac += time;
		}
		return ac / runs;
	}

	public long get(String phase, int run, String dataset) {
		Map<Integer, Map<String, Long>> phaseMap = table.get(phase);
		if (phaseMap == null)
			return -1;
		Map<String, Long> runMap = phaseMap.get(run);
		if (runMap == null)
			return -1;
		Long time = runMap.get(dataset);
		if (time == null)
			return -1;
		return time;
	}

	public void put(String phase, int run, String dataset, Long time) {
		Map<Integer, Map<String, Long>> phaseMap = table.get(phase);
		if (phaseMap == null) {
			phaseMap = new HashMap<Integer, Map<String, Long>>();
			table.put(phase, phaseMap);
			phases.add(phase);
		}
		Map<String, Long> runMap = phaseMap.get(run);
		if (runMap == null) {
			runMap = new HashMap<String, Long>();
			phaseMap.put(run, runMap);
		}
		if (run > runs)
			runs = run;
		if (!datasets.contains(dataset)) 	
			datasets.add(dataset);	
		runMap.put(dataset, time);
	}

	public void save() {
		Charset charset = Charset.forName("US-ASCII");
		Path file = FileSystems.getDefault().getPath(name + ".csv");
		try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
			for (String dataset : datasets) {
				writer.write(",");
				writer.write(dataset);
			}
			writer.newLine();
			for (String phase : phases) {
				for (int run = 1; run <= runs; run++) {
					writer.write(phase);
					for (String dataset : datasets) {
						long time = get(phase, run, dataset);
						writer.write(",");
						writer.write(time == -1 ? "-" : String.valueOf(time));
					}
					writer.newLine();
				}
//				writer.write(phase);
//				writer.write(",");
//				writer.write("average");
//				for (String dataset : datasets) {
//					long average = average(phase, dataset);
//					writer.write(",");
//					writer.write(average == -1 ? "-" : String.valueOf(average));
//				}
//				writer.newLine();
			}
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}
}