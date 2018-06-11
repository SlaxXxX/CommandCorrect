package de.minetropolis.dedicated;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.minetropolis.messages.ConsoleReceiver;
import de.minetropolis.process.Config;
import de.minetropolis.process.CorrectionProcess;
import de.minetropolis.process.ProcessExecutor;

public class DedicatedCorrector implements ProcessExecutor {
	static private boolean appendLines = false;
	private CorrectionProcess myProcess;

	File folder;
	File[] content;

	public static void main(String[] args) {
		if (args.length > 0)
			if (args[0].equals("append"))
				appendLines = true;
		try {
			new DedicatedCorrector();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DedicatedCorrector() {
		Config.loadConfig();
		try {
			folder = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toURI().getPath(), "CommandCorrector");
			content = new File(folder.toURI().getPath(), "Dedicated").listFiles();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		if (content.length == 0) {
			System.out.println("Dedicated Corrector: \"CommandCorrector\\Dedicated\" folder does not contain any files");
			return;
		}
		System.out.print("Dedicated Corrector: Found " + content.length + " Files: ");
		System.out.println(Arrays.asList(content).stream().map(File::getName).collect(Collectors.joining(" ,")));
		System.out.println("\n");
		for (File file : content) {
			System.out.println("Dedicated Corrector: Correcting " + file.getName());
			List<String> fillLines = new ArrayList<>();
			try {
				fillLines = Files.readAllLines(file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (appendLines)
				fillLines = Arrays.asList(fillLines.stream().collect(Collectors.joining("\n")));
			if (fillLines.size() > 0) {
				myProcess = new CorrectionProcess(this, new ConsoleReceiver(), file.getName());
				new Thread(myProcess.process(fillLines)).start();
			}
		}
	}

	@Override
	public void collectFinished(String id, List<String> strings) {
		System.out.println("Dedicated Corrector: Finished " + id);
		for (File file : content) {
			if (file.getName().equals(id)) {
				file.delete();
				OutputStreamWriter fileWriter;
				try {
					fileWriter = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);

					String output = strings.stream().map(line -> line = line.replaceAll(";\\\\|\n", System.lineSeparator())).collect(Collectors.joining(System.lineSeparator()));

					fileWriter.write(output);
					fileWriter.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}