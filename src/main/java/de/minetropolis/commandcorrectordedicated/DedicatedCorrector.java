package de.minetropolis.commandcorrectordedicated;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.minetropolis.commandcorrectorutil.InterpretedPattern;
import de.minetropolis.commandcorrectorutil.Notification;
import de.minetropolis.commandcorrectorutil.Statics;

public class DedicatedCorrector {
    static boolean appendLines = false;

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

    private DedicatedCorrector() throws Exception {
        System.out.println("Dedicated Corrector: Loading Config");
        List<InterpretedPattern> list = Statics.loadConfig();
        System.out.println("Dedicated Corrector: Found " + list.size() + " ChangeRules");
        File folder = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toURI().getPath(), "CommandCorrector");
        File[] content = new File(folder.toURI().getPath(), "Dedicated").listFiles();
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
                file.delete();
                OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8);
                fileWriter.write(getNewContent(fillLines, list));
                fileWriter.close();
            }
        }
    }

    private String getNewContent(List<String> lines, List<InterpretedPattern> list) {
        List<String> returnString = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String newline = lines.get(i);
            final int finalI = i;
            for (InterpretedPattern ip : list) {
                Notification notification = Statics.notify(Statics.changeCommand(ip, newline));
                notification.entries.forEach(notif -> System.out.println("Line " + finalI + " notifies: " + notif.message + ", at: " + notif.normalText));
                newline = notification.command;
                newline = newline.replaceAll(";\\\\|\n", System.lineSeparator());
            }
            returnString.add(newline);
        }
        return returnString.stream().collect(Collectors.joining(System.lineSeparator()));
    }
}