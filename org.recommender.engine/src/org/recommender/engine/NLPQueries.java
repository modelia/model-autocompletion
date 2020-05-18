package org.recommender.engine;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class NLPQueries {
	Process mProcess;

	public static void main(String[] args) {
		NLPQueries scriptPython = new NLPQueries();
		scriptPython.runScript("python_scripts/NLP_Components.py", "arg1 arg2");
	}

	public void runScript(String pyFile, String args) {
		Process process;
		try {
			process = Runtime.getRuntime().exec( "py " + pyFile + " " + args );
			mProcess = process;
		} catch (Exception e) {
			System.out.println("Exception Raised" + e.toString());
		}
		InputStream stdout = mProcess.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				System.out.println("stdout: " + line);
			}
		} catch (IOException e) {
			System.out.println("Exception in reading output" + e.toString());
		}
	}

}

