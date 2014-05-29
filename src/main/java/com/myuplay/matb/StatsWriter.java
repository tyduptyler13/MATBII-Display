package com.myuplay.matb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javafx.concurrent.Task;

public class StatsWriter extends Task<String> {

	private final File file;
	private final List<Trial> trials;

	public StatsWriter(File out, List<Trial> trials) {
		super();
		file = out;
		this.trials = trials;
	}

	@Override
	protected String call() throws Exception {

		BufferedWriter out = null;

		try {

			out = new BufferedWriter(new FileWriter(file));

			out.append(Trial.getStatsHeader() + "\r\n"); // DOS formated.

			for (Trial t : trials) {
				t.getStats(out);
			}

		} catch (Exception e) {

			super.failed();
			updateMessage("Failed: " + e.getMessage());
			e.printStackTrace(System.err);
			return "Failed to save file! (" + file.getName() + ")";

		} finally {
			out.flush();
			out.close();
		}

		return "Printed data to " + file.getName();
	}

}
