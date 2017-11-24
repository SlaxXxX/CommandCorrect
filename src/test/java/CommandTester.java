import de.minetropolis.commandcorrector.CommandCorrector;
import de.minetropolis.commandcorrector.CommandblockCorrectCommand;

public class CommandTester {
	private String[] commands = {
//			"/setblock ~1 ~5 ~3 command_block 5 replace {Command:\"/say Ein Test!\"}",
//			"/setblock ~ ~-5 ~3 command_block",
//			"setblock ~1 ~5 ~3 command_block 2 replace",
//			"setblock 14 -39 118 minecraft:stone facing=north"
			
//			"@a[test1,score_test_min=1,test2,score_test=3,test3]",
//			"@e[score_test=43, test,score_test_min=2,test]",
//			"@r[test,score_test=1]",
//			"@p[score_test_min=14]",
//			"@a[scores=[nochntest=3..],score_test_min=1,test,score_test=3]",
//			"@e[scores=[nochntest=1],score_test=43, test,score_test_min=2,test]",
//			"@r[scores=[nochntest=3..36],test,score_test=1]",
//			"@p[scores=[nochntest=..6],score_test_min=14]"
	};
	private String[][] rules = {
//			{
//					";?(?:\\/?)setblock ;?((?:~?-?\\d* *){3});?((?:minecraft\\:)?\\w+);?(?: *);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: *);?((?:\\w+)?);?(?: *);?((?:\\{.+\\})?)",
//					"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)"
//			}
			{
				"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(\\d+);?(.*);?(?:,? *);?(?:,)score_;?(\\3)=;?(\\d+);?(.*)]",
				"@;:(1)[;:(2)scores=[;:(3)=;:(4)..;:(7)];:(5);:(8)]",
				""
			},
			{
				"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(\\d+);?(.*);?(?:,? *);?(?:,)score_;?(\\3)_min=;?(\\d+);?(.*)]",
				"@;:(1)[;:(2)scores=[;:(3)=;:(7)..;:(4)];:(5);:(8)]",
				""
			},
			{
				"@;?(\\w)[;?(.*)score_;?(\\w+)_min=;?(\\d+);?(.*)]",
				"@;:(1)[;:(2)scores=[;:(3)=;:(4)..];:(5)]",
				""
			},
			{
				"@;?(\\w)[;?(.*)score_;?(\\w+)=;?(\\d+);?(.*)]",
				"@;:(1)[;:(2)scores=[;:(3)=..;:(4)];:(5)]",
				""
			},
	};

	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {
		CommandblockCorrectCommand cbc = new CommandblockCorrectCommand(null);

		for (int i = 0; i < commands.length; i++) {
			for (String[] rule : rules) {
				commands[i] = cbc.notify(" TEST", cbc.changeCommand(commands[i], CommandCorrector.interpretPattern(rule[0]), rule[1], rule[2]));
				System.out.println(commands[i] + "\n");
			}
			System.out.println("\n--------------\n");
		}
	}
}
