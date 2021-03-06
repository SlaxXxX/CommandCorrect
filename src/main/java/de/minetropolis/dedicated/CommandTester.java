package de.minetropolis.dedicated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.minetropolis.newutil.InterpretedPattern;
import de.minetropolis.newutil.Notification;
import de.minetropolis.newutil.NotificationEntry;
import de.minetropolis.newutil.Statics;

public class CommandTester {
	private String[] commands = {
		//            		"/setblock ~1 ~5 ~3 command_block 5 replace {Command:\"/say Ein Test!\"}",
		//            		"/setblock ~ ~-5 ~3 command_block",
		//            		"setblock ~1 ~5 ~3 command_block 2 replace",
		//            		"setblock 14 -39 118 minecraft:stone facing=north"

//				"@a[test1,score_test_min=-1,test2,score_test=3,test3]",
//				"@e[score_test=43, test1,score_test_min=2,test2]",
//				"@r[test1,score_test=1]",
//				"@p[score_test_min=14]",
//				"@a[test1,score_test_min=1,test2,score_test=3,test3,scores=[nochntest=3..],test4]",
//				"@e[score_test=43, test1,score_test_min=2,scores=[nochntest=1],test2]",
//				"@r[test1,test2,score_test=1,scores=[nochntest=3..36]]",
//				"@p[score_test_min=14,scores=[nochntest=..6]]",
//				"@a[test1, score_test_min=-1, test2, score_test=3, test3, score_nochntest=2, test4, score_nochntest_min=1, test5, score_wienochntest=4, test6, score_letztertest_min=-13, score_letztertest=48, test7, score_jetzaberletztertest_min=0, test8, score_jetzaberletztertest=1, test9]"

//		"@a[g=1]",
//		"@a[g=s]",
//		"@p[g=spectator]",
//		"@r[gamemode=2]",
//		"@s[gamemode=sp]",
//		"@r[gamemode=creative]"

//		            "This is a test",
//		            "This is a fail"
			"score_test_min=X,score_test=X"

	};
	private String[][] rules = {
//            		{
//            			";?(?:\\/?)setblock ;?((?:~?-?\\d* *){3});?((?:minecraft\\:)?\\w+);?(?: *);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: *);?((?:\\w+)?);?(?: *);?((?:\\{.+\\})?)",
//            			"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)",
//            			""
//            		}
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?:, *);?(.*)score_;?(?:\\3)=;?(-?\\d+);?(.*)]",
//            			"@;:(1)[;:(2);:(5)scores=[;:(3)=;:(4)..;:(6)];:(7)]",
//            			"(scores=\\[.*\\])"
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?:, *);?(.*)score_;?(?:\\3)_min=;?(-?\\d+);?(.*)]",
//            			"@;:(1)[;:(2);:(5)scores=[;:(3)=;:(6)..;:(4)];:(7)]",
//            			"(scores=\\[.*\\])"
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(.*)]",
//            			"@;:(1)[;:(2)scores=[;:(3)=;:(4)..];:(5)]",
//            			"(scores=\\[.*\\])|(score_\\w+(?!_min).{4}=-?\\d+)"
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(.*)]",
//            			"@;:(1)[;:(2)scores=[;:(3)=..;:(4)];:(5)]",
//            			"(scores=\\[.*\\])|(score_\\w+_min=-?\\d+)"
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?: *,);?(.*)score_;?(?:\\3)=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
//            			"@;:(1)[;:(2);:(5);:(7);:(8),;:(3)=;:(4)..;:(6)];:(9)]",
//            			""
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?: *,);?(.*)score_;?(?:\\3)_min=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
//            			"@;:(1)[;:(2);:(5);:(7);:(8),;:(3)=;:(6)..;:(4)];:(9)]",
//            			""
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
//            			"@;:(1)[;:(2);:(5);:(6),;:(3)=;:(4)..];:(7)]",
//            			"(score_\\w+(?!_min).{4}=-?\\d+)"
//            		},
//            		{
//            			"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(-?\\d+);?(?: *,);?(.*);?(scores=\\[.*)];?(.*)]",
//            			"@;:(1)[;:(2);:(5);:(6),;:(3)=;:(4)..];:(7)]",
//            			"(score_\\w+_min=-?\\d+)"
//            		}
//            {
//            	";?([\\[,]{1});?(?: *);>(g|gamemode)<;;?(?: *)=;?(?: *);>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;;?(?: *);?([\\],]{1})",
//            	";:(1);:(2)=;:(3);:(4)",
//            	""
//            }
//		{
//			";>(\"\\[g=1\\]\"|\"\\[gamemode=creative\\]\")<;",
//			";:(1)",
//			""
//		}

//            {
//                    "This is a ;?(.*)",
//                    "This is ;:1(a ;:(1)):(test):;;:1(no test):!(test):;",
//                    ""
//            }
//		{
//			";>(?:.*TEST.*|TEST CASE)|(?:.+-(\\d+)|ID-\\2)|(?:.+ (\\d+)\\.(\\d+)|PGPH \\3\\.\\4)<;",
//			"New value is:  ;:(1)",
//			""
//		}
			{
				"score_test_min=X,score_test=X",
				";*(test,0)score_test_min=;+(test,0),score_test=;+(test,1)",
				""
			}
    };

	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {
		String result;

		for (int num = 1; num <= 1; num++) {
			System.out.println("--- RUN" + num + " ---");

			List<InterpretedPattern> patterns = new ArrayList<>();
			for (String[] rule : rules) {
				patterns.add(new InterpretedPattern(rule[0], rule[1], rule[2]).compile());
			}
			Map<String,Double> counters = Statics.initCounters(patterns);

			for (int i = 0; i < commands.length; i++) {
				System.out.println(commands[i]);
				for (InterpretedPattern ip : patterns) {
					Notification notification = Statics.notify(Statics.changeCommand(ip, commands[i], counters));
					for (NotificationEntry entry : notification.entries)
						System.out.println("Command " + i + " notifies: " + entry.message + "; at -> " + entry.normalText);
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
