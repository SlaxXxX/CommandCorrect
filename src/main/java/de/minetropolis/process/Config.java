package de.minetropolis.process;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.minetropolis.process.InterpretedPattern;

public class Config {
	
	public static List<InterpretedPattern> patterns = new ArrayList<>();
	public static IPCounters counters;
	
	public static void loadConfig() {
		File jar = null;
		try {
			jar = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File config = new File(new File(jar.getParentFile().toURI().getPath(), "CommandCorrector").toURI().getPath(), "config.yml");

		if (!config.exists() || config.isDirectory()) {
			config.getParentFile().mkdirs();
			new File(config.getParent(), "Dedicated").mkdir();
			try {
				Files.copy(Config.class.getResourceAsStream("/config.yml"), Paths.get(config.toURI()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			StringBuilder sb = new StringBuilder();
			Files.readAllLines(config.toPath()).forEach(string -> sb.append(string).append("\n"));
			patterns = processFile(sb.toString());
			counters = new IPCounters(patterns);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<InterpretedPattern> processFile(String string) {
		List<InterpretedPattern> list = new ArrayList<>();
		Matcher matcher = Pattern.compile("(?<=^|\\n)[ \\t]*\"(.+)\"[ \\t]*\\n?[ \\t]*:[ \\t]*\\n?[ \\t]*\"(.*?)\"(?:[ \\t]*\\n?[ \\t]*\\|[ \\t]*\\n?[ \\t]*\"(.*)\")?[ \\t]*(?=$|\\n)").matcher(string);
		while (matcher.find()) {
			InterpretedPattern pattern = new InterpretedPattern(matcher.group(1), matcher.group(2), (matcher.group(3) == null ? "" : matcher.group(3))).compile();
			if (pattern != null)
				list.add(pattern);
		}
		return list;
	}
}
