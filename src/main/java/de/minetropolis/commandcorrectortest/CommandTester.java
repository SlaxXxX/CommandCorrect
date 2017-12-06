package de.minetropolis.commandcorrectortest;

import java.util.Arrays;

import de.minetropolis.commandcorrector.CommandCorrector;
import de.minetropolis.commandcorrector.CommandblockCorrectCommand;
import de.minetropolis.commandcorrector.Notification;
import de.minetropolis.commandcorrector.NotificationEntry;
import de.minetropolis.commandcorrector.Statics;

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
		"@a[test1,score_test_min=1,test2,score_test=3,test3,scores=[nochntest=3..],test4]",
		"@e[score_test=43, test1,score_test_min=2,scores=[nochntest=1],test2]",
		"@r[test1,test2,score_test=1,scores=[nochntest=3..36]]",
		"@p[score_test_min=14,scores=[nochntest=..6]]",
		"@a[test1,score_test_min=-1,test2,score_test=3,test3,score_nochntest=2,test4,score_nochntest_min=1,test5,score_wienochntest=4,test6,score_letztertest_min=-13,score_letztertest=48,test7,score_jetzaberletztertest_min=0,test8,score_jetzaberletztertest=1,test9]"

	};
	private String[][] rules = {
		//			{
		//					";?(?:\\/?)setblock ;?((?:~?-?\\d* *){3});?((?:minecraft\\:)?\\w+);?(?: *);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: *);?((?:\\w+)?);?(?: *);?((?:\\{.+\\})?)",
		//					"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)"
		//			}
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?:, *);?(.*)score_;?(?:\\3)=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2);:(5)scores=[;:(3)=;:(4)..;:(6)];:(7)]",
			"(scores=\\[.*\\])"
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?:, *);?(.*)score_;?(?:\\3)_min=;?(-?\\d+);?(.*)]",
			"@;:(1)[;:(2);:(5)scores=[;:(3)=;:(6)..;:(4)];:(7)]",
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
			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?: *,);?(.*)score_;?(?:\\3)=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
			"@;:(1)[;:(2);:(5);:(7);:(8),;:(3)=;:(4)..;:(6)];:(9)]",
			""
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?: *,);?(.*)score_;?(?:\\3)_min=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
			"@;:(1)[;:(2);:(5);:(7);:(8),;:(3)=;:(6)..;:(4)];:(9)]",
			""
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
			"@;:(1)[;:(2);:(5);:(6),;:(3)=;:(4)..];:(7)]",
			"(score_\\w+(?!_min).{4}=-?\\d+)"
		},
		{
			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
			"@;:(1)[;:(2);:(5);:(6),;:(3)=;:(4)..];:(7)]",
			"(score_\\w+_min=-?\\d+)"
		}
	};

	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {
		String result;

		for (int num = 1; num <= 3; num++) {
			System.out.println("--- RUN" + num + " ---");
			for (int i = 0; i < commands.length; i++) {
				for (String[] rule : rules) {
					Notification notification = Statics.notify(Statics.changeCommand(commands[i], Statics.interpretPattern(rule[0]), rule[1], rule[2]));
					for (NotificationEntry entry : notification.entries)
						System.out.println("Command " + i + " notifies: " + entry.message + "; at -> " + entry.hoverText);
					result = notification.command;
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
