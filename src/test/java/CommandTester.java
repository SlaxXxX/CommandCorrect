import java.util.Map;

import org.bukkit.block.CommandBlock;

import de.minetropolis.commandcorrector.CommandCorrector;
import de.minetropolis.commandcorrector.CommandblockCorrectCommand;

public class CommandTester {
	//
	public static void main(String[] args) {
		new CommandTester();
	}

	private CommandTester() {

		CommandblockCorrectCommand cbc = new CommandblockCorrectCommand();
		System.out.println(
				cbc.notify(" TEST", cbc.changeCommand("/setblock ~1 ~5 ~3 command_block 5 replace {Command:\"/say Ein Test!\"}",
				CommandCorrector.interpretPattern("setblock ;?((?:~?\\d+ ){3});?(\\w+) ;?(\\d+) ;?(\\w+) ;?(\\{.+\\})"),
				"setblock ;:(1);:(2)[;!(Add Block State);:(3)];:(5) ;:(4)"))
		);
	}
	//
}
