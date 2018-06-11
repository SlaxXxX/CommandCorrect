package de.minetropolis.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.minetropolis.process.InterpretedPattern;

public class IPCounters {
	private Map<String,Double> counters = new HashMap<>();
	
	public Map<String,Double> clone(){
		Map<String,Double> clone = new HashMap<>();
		for(Entry<String, Double> entry : counters.entrySet()) {
			clone.put(entry.getKey(), (double)entry.getValue());
		}
		return clone;
	}
	
	public IPCounters(List<InterpretedPattern> patterns) {
		for (InterpretedPattern ip : patterns) {
			Matcher matcher = Pattern.compile(";\\*\\(([;\\w]+),((?:-?\\d+(?:\\.\\d+)?)|(?:-?\\d+\\/\\d+))\\)").matcher(ip.target);
			while (matcher.find()) {
				if (!counters.containsKey(matcher.group(1)))
					counters.put(matcher.group(1), parseDouble(matcher.group(2)));
				ip.target = ip.target.replace(matcher.group(), "");
			}
		}
	}

	public static double parseDouble(String str) {
		String[] fraction = str.split("\\/");
		if (fraction.length == 2)
			return Double.parseDouble(fraction[0]) / Double.parseDouble(fraction[1]);
		else
			return Double.parseDouble(str);
	}
}
