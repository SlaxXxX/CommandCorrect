package de.minetropolis.commandcorrectortest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.minetropolis.commandcorrector.CommandCorrector;
import de.minetropolis.commandcorrector.CommandblockCorrectCommand;

public class CommandTester {
	private String[] commands = {
		//			"/setblock ~1 ~5 ~3 command_block 5 replace {Command:\"/say Ein Test!\"}",
		//			"/setblock ~ ~-5 ~3 command_block",
		//			"setblock ~1 ~5 ~3 command_block 2 replace",
		//			"setblock 14 -39 118 minecraft:stone facing=north"

		"@a[test1,score_test_min=-1,test2,score_test=3,test3]",
		"@e[score_test=43, test1,score_test_min=2,test2]",
		"@r[test1,score_test=1]",
		"@p[score_test_min=14]",
		"@a[scores=[nochntest=3..],test1,score_test_min=1,test2,score_test=3,test3]",
		"@e[scores=[nochntest=1],score_test=43, test1,score_test_min=2,test2]",
		"@r[test1,scores=[nochntest=3..36],test2,score_test=1]",
		"@p[scores=[nochntest=..6],score_test_min=14]",
		"@a[test1,score_test_min=-1,test2,score_test=3,test3,score_nochntest=2,test4,score_nochntest_min=1,test5,score_wienochntest=4,test6,score_letztertest_min=-13,score_letztertest=48,test7]"
		
	};
	private String[][] rules = {
		//			{
		//					";?(?:\\/?)setblock ;?((?:~?-?\\d* *){3});?((?:minecraft\\:)?\\w+);?(?: *);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: *);?((?:\\w+)?);?(?: *);?((?:\\{.+\\})?)",
		//					"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)"
		//			}
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(.*);?(?:, *)score_;?(?:\\3)=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2)scores=[;:(3)=;:(4)..;:(6)];:(5);:(7)]",
			"(scores=\\[.*\\])"
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(.*);?(?:, *)score_;?(?:\\3)_min=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2)scores=[;:(3)=;:(6)..;:(4)];:(5);:(7)]",
			"(scores=\\[.*\\])"
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2)scores=[;:(3)=;:(4)..];:(5)]",
			"(scores=\\[.*\\])|(score_\\w+(?!_min).{4}=-?\\d+)"
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2)scores=[;:(3)=..;:(4)];:(5)]",
			"(scores=\\[.*\\])|(score_\\w+_min=-?\\d+)"
		},
		{
			"@;?(\\w)[;?(.*);?(scores=\\[.*)];?(.*);?(?:, *)score_;?(\\w+)_min=;?(-?\\d+);?(.*);?(?:, *)score_;?(?:\\5)=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2);:(3),;:(5)=;:(6)..;:(8)];:(4);:(7);:(9)]",
			""
		},
		{
			"@;?(\\w)[;?(.*);?(scores=\\[.*)];?(.*);?(?:, *)score_;?(\\w+)=;?(-?\\d+);?(.*);?(?:, *)score_;?(?:\\5)_min=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2);:(3),;:(5)=;:(8)..;:(6)];:(4);:(7);:(9)]",
			""
		},
		{
			"@;?(\\w)[;?(.*);?(scores=\\[.*)];?(.*);?(?:, *)score_;?(\\w+)_min=;?(-?\\d+);?(.*);?(.*)]",
			"@;:(1)[;:(2);:(3),;:(5)=;:(6)..];:(4);:(7)]",
			"(score_\\w+(?!_min).{4}=-?\\d+)"
		},
		{
			"@;?(\\w)[;?(.*);?(scores=\\[.*)];?(.*);?(?:, *)score_;?(\\w+)=;?(-?\\d+);?(.*);?(.*)]",
			"@;:(1)[;:(2);:(3),;:(5)=..;:(6)];:(4);:(7)]",
			"(score_\\w+_min=-?\\d+)"
		}
	};

	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {
		CommandblockCorrectCommand cbc = new CommandblockCorrectCommand(null);
		String result;

		for (int num = 1; num <= 3; num++) {
			System.out.println("--- RUN" + num + " ---");
			for (int i = 0; i < commands.length; i++) {
				for (String[] rule : rules) {
					result = cbc.notify(" TEST", cbc.changeCommand(commands[i], CommandCorrector.interpretPattern(rule[0]), rule[1], rule[2]));
					if (commands[i] != result)
						System.out.println(result + "\n");
					commands[i] = result;
				}
				System.out.println("\n--------------\n");
			}
		}
		
		System.out.println("\n----- END RESULT -----\n");
		Arrays.asList(commands).forEach(System.out::println);
	}
}
