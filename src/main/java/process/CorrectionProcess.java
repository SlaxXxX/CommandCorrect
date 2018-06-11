package process;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.minetropolis.newutil.InterpretedPattern;
import de.minetropolis.newutil.Statics;

public class CorrectionProcess implements Runnable {

	List<InterpretedPattern> patterns;
	List<String> strings;
	List<String> newStrings;
	ProcessExecutor executor;
	String id;

	public CorrectionProcess(ProcessExecutor exec, String id) {
		this.id = id;
		executor = exec;
	}

	@Override
	public void run() {
		for (String string : strings) {
			String newString = "";
			for (InterpretedPattern ip : patterns) {
				newString = correctString(ip, string);
			}
			newStrings.add(newString);
		}
		executor.collectFinished(id, newStrings);
	}

	public CorrectionProcess process(List<String> strings, String pattern, String target, String assertion) {
		patterns = new ArrayList<>();
		patterns.add(new InterpretedPattern(pattern, target, assertion).compile());
		this.strings = strings;
		return this;
	}

	public CorrectionProcess process(List<String> strings) {
		patterns = loadConfig();
		this.strings = strings;
		return this;
	}

	private List<InterpretedPattern> loadConfig() {
		File jar = null;
		try {
			jar = new File(Statics.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		File config = new File(new File(jar.getParentFile().toURI().getPath(), "CommandCorrector").toURI().getPath(), "config.yml");

		if (!config.exists() || config.isDirectory()) {
			config.getParentFile().mkdirs();
			new File(config.getParent(), "Dedicated").mkdir();
			try {
				Files.copy(Statics.class.getResourceAsStream("/config.yml"), Paths.get(config.toURI()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			StringBuilder sb = new StringBuilder();
			Files.readAllLines(config.toPath()).forEach(string -> sb.append(string).append("\n"));
			return processFile(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private List<InterpretedPattern> processFile(String string) {
		List<InterpretedPattern> list = new ArrayList<>();
		Matcher matcher = Pattern.compile("(?<=^|\\n)[ \\t]*\"(.+)\"[ \\t]*\\n?[ \\t]*:[ \\t]*\\n?[ \\t]*\"(.*?)\"(?:[ \\t]*\\n?[ \\t]*\\|[ \\t]*\\n?[ \\t]*\"(.*)\")?[ \\t]*(?=$|\\n)").matcher(string);
		while (matcher.find()) {
			InterpretedPattern pattern = new InterpretedPattern(matcher.group(1), matcher.group(2), (matcher.group(3) == null ? "" : matcher.group(3))).compile();
			if (pattern != null)
				list.add(pattern);
		}
		return list;
	}

	private String correctString(InterpretedPattern ip, String string) {
		return string;
	}
}
