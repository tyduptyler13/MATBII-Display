package com.myuplay.matb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javafx.concurrent.Task;

/**
 * Concurrent method for writing files.
 * 
 * Returns status message when finished.
 * 
 * @author Tyler
 * 
 */
public class DataWriter extends Task<String> {

	private final File file;
	private final List<Trial> trials;

	public DataWriter(File out, List<Trial> trials) {
		super();
		file = out;
		this.trials = trials;
	}

	@Override
	protected String call() throws Exception {

		BufferedWriter out = null;

		try {

			out = new BufferedWriter(new FileWriter(file));

			out.append(Trial.getHeader() + "\r\n"); // DOS formated.

			for (Trial t : trials) {
				t.toString(out);
			}

		} catch (Exception e) {

			failed();
			setException(e);
			updateMessage("Failed");
			e.printStackTrace(System.err);
			return "Failed to save file (" + file.getName() + "): "
					+ e.getMessage();

		} finally {
			out.flush();
			out.close();
		}

		return "Printed data to " + file.getName();
	}

}