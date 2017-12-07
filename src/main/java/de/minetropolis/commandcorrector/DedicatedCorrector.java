package de.minetropolis.commandcorrector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DedicatedCorrector {

	public static void main(String[] args) {
		try {
			new DedicatedCorrector();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DedicatedCorrector() throws Exception {
		System.out.println("Dedicated Corrector: Loading Config");
		Map<String, List<String>> map = Statics.loadConfig();
		File folder = null;
		folder = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toURI().getPath(), "CommandCorrector");
		File[] content = new File(folder.toURI().getPath(), "Dedicated").listFiles();
		if (content.length == 0) {
			System.out.println("Dedicated Corrector: Input does not contain any files");
			return;
		}
		for (File file : content) {
			System.out.println("Dedicated Corrector: Correcting " + file.getName());
			List<String> fillLines = new ArrayList<>();
			try {
				fillLines = Files.readAllLines(file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			final List<String> lines = Collections.unmodifiableList(fillLines);
			if (lines.size() > 0) {
				file.delete();
				FileWriter fileWriter = new FileWriter(file, true);

				for (String line : lines) {
					String newline = line;
					for (Entry<String, List<String>> entry : map.entrySet()) {
						Notification notification = Statics.notify(Statics.changeCommand(newline, Statics.interpretPattern(entry.getKey()), entry.getValue().get(0), entry.getValue().get(1)));
						notification.entries.forEach(notif -> System.out.println("Line " + lines.indexOf(line) + " notifies: " + notif.message + ", at: " + notif.normalText));
						newline = notification.command;
					}
					fileWriter.write(newline);
					if (lines.indexOf(line) < lines.size() - 1)
						fileWriter.append(System.lineSeparator());
				}
				fileWriter.close();
			}
		}
	}
}
