package com.Scheduler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RunDataJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		System.out.println("Reading data");
		ReadData();

	}

	private void ReadData() {
		try {
			Data.br = new BufferedReader(new FileReader(Data.csvFile));
			List<String> lines = Data.br.lines().skip(Data.last).limit(10).collect(Collectors.toList());
			for (String l : lines) {
				String[] p = l.split(Data.cvsSplitBy);
				System.out.println("GreenHouse [name= " + p[2] + ", TInt =" + p[8] + ", TExt = " + p[3] + ", MInt= "
						+ p[11] + " ]");
			}

			Data.last = Data.last + 10;
			lines.clear();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (Data.br != null) {
				try {
					Data.br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}