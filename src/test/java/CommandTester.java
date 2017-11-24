import de.minetropolis.commandcorrector.CommandCorrector;
import de.minetropolis.commandcorrector.CommandblockCorrectCommand;

public class CommandTester {
	private String[] commands = {
			"/setblock ~1 ~5 ~3 command_block 5 replace {Command:\"/say Ein Test!\"}",
			"/setblock ~ ~-5 ~3 command_block",
			"setblock ~1 ~5 ~3 command_block 2 replace",
			"setblock 14 -39 118 minecraft:stone facing=north"
			};
	private String[][] rules = {
			{
				";?(?:\\/?)setblock ;?((?:~?-?\\d* ){3});?((?:minecraft\\:)?\\w+);?(?: ?);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: ?);?((?:\\w+)?);?(?: ?);?((?:\\{.+\\})?)",
				"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)"
			}
			};

	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {
//		CommandblockCorrectCommand cbc = new CommandblockCorrectCommand(null,null);
//
//		for (int i = 0; i < commands.length; i++) {
//			for (String[] rule : rules) {
//				commands[i] = cbc.notify(" TEST", cbc.changeCommand(commands[i], CommandCorrector.interpretPattern(rule[0]), rule[1]));
//				System.out.println(commands[i] + "\n");
//			}
//			System.out.println("\n--------------\n");
//		}
	}
}
