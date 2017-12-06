package de.minetropolis.commandcorrector;

import java.util.List;
import java.util.Map;

public class DedicatedCorrector {

	public static void main(String[] args) {
		new DedicatedCorrector();
	}

	private DedicatedCorrector() {
		Map<String, List<String>> map = Statics.loadConfig();
		System.out.println("test");
	}
}
