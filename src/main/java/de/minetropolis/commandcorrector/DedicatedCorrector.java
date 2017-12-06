package de.minetropolis.commandcorrector;

import java.util.List;
import java.util.Map;

public class DedicatedCorrector {

	public static void main(String[] args) {
		new DedicatedCorrector();
	}

	private DedicatedCorrector() {
		System.out.println("Dedicated Corrector: Loading Config");
		Map<String, List<String>> map = Statics.loadConfig();
	}
}
