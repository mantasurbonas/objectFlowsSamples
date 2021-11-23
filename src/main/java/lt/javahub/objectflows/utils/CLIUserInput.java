package lt.javahub.objectflows.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CLIUserInput {
	
	private BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));

	public boolean askBoolean(String question) {
		System.out.print(question + " (y/n):");
		return readLine().equalsIgnoreCase("y");
	}

	public String askString(String question) {
		System.out.print(question + " : ");
		return readLine();
	}

	private String readLine() {
		try {
			return stdinReader.readLine().trim();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
