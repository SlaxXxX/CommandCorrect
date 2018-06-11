import java.util.ArrayList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import de.minetropolis.groups.*;
import de.minetropolis.messages.ConsoleReceiver;
import de.minetropolis.process.InterpretedPattern;
import de.minetropolis.process.CorrectionProcess;
import de.minetropolis.process.ProcessExecutor;
import junit.framework.Assert;

public class UnitTest extends Assert implements ProcessExecutor {
	String currentExpected = "";

	@Rule
	public ErrorCollector ec = new ErrorCollector();

	@Test
	public void testAutoconvertGroup() {
		equals("((?:))", new Autoconvert(";>()<;").apply());
		equals("((?:a|b|c))", new Autoconvert(";>(a|b|c)<;").apply());
		equals("((?:)|(?:))", new Autoconvert(";>()|()<;").apply());
		equals("((?:a|b|c)|(?:a|b|c))", new Autoconvert(";>(a|b|c)|(a|b|c)<;").apply());
		equals("((?:(.*)|test)|(?:.*|\\1))", new Autoconvert(";>((.*)|test)|(.*|\\1)<;").apply());
	}

	@Test
	public void testEscapedGroup() {
		equals("", new Escaped("").apply());
		equals("test", new Escaped("test").apply());
		equals("@a\\[g=3,scores=\\[test=1\\.\\.3,boob=4\\.\\.\\]\\]", new Escaped("@a[g=3,scores=[test=1..3,boob=4..]]").apply());
	}

	@Test
	public void testNoncapturingGroup() {
		equals("(?:test)", new Noncapturing(";?(?:test)").apply());
		equals("(?:())", new Noncapturing(";?(?:())").apply());
	}

	@Test
	public void testNormalGroup() {
		equals("(test)", new Normal("(test)").apply());
		equals("(())", new Normal("(())").apply());
	}

	@Test
	public void testSpecialGroup() {
		equals("(?:test)", new Special(";?(?:test)").apply());
		equals("(?:())", new Special(";?(?:())").apply());
	}

	@Test
	public void testInterpretedPattern() {
		InterpretedPattern ip = new InterpretedPattern(";?([\\[,]{1});?(?: *);>(g|gamemode)<;;?(?: *)=;?(?: *);>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;;?(?: *);?([\\],]{1})").compile();
		String[] expected = {
			"([\\[,]{1})(?: *)((?:g|gamemode))(?: *)=(?: *)((?:0|s|survival)|(?:1|c|creative)|(?:2|a|adventure)|(?:3|sp|spectator))(?: *)([\\],]{1})",
			"([\\[,]{1})",
			"(?: *)",
			"((?:g|gamemode))",
			"(?: *)",
			"=",
			"(?: *)",
			"((?:0|s|survival)|(?:1|c|creative)|(?:2|a|adventure)|(?:3|sp|spectator))",
			"(?: *)",
			"([\\],]{1})"
		};
		equals(expected[0], ip.pattern);
		for (int i = 1; i < expected.length; i++)
			equals(expected[i], ip.groups.get(i - 1).getContent());

		ip = new InterpretedPattern(";?(a(b)c(d(e)f(g)h)i)").compile();
		String[] expected1 = {
			"(a(b)c(d(e)f(g)h)i)",
			"(a(b)c(d(e)f(g)h)i)",
			"(b)",
			"(d(e)f(g)h)",
			"(e)",
			"(g)"
		};
		equals(expected1[0], ip.pattern);
		for (int i = 1; i < expected1.length; i++)
			equals(expected1[i], ip.groups.get(i - 1).getContent());
	}

	@Test
	public void testProcess() {
		InterpretedPattern ip = new InterpretedPattern(";?([\\[,]{1});?(?: *);>(g|gamemode)<;;?(?: *)=;?(?: *);>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;;?(?: *);?([\\],]{1})", ";:(1);:(2)=;:(3);:(4)", "").compile();
		createProcess("@a[gamemode=survival]", "@a[g=s]", ip);
		createProcess("@s[gamemode=spectator]", "@s[gamemode=sp]", ip);
		createProcess("[A]", "(A)", new InterpretedPattern(";>(\\((\\w)\\)|\\[\\2\\])<;", ";:(1)", "").compile());
		createProcess("\\Test\\", "Test", new InterpretedPattern(";>(Test|\\\\Test\\\\)<;", ";:(1)", "").compile());
		
		ip = new InterpretedPattern(";?(\\d)",";*(inc,10);+(inc,1)","").compile();
		createProcess("1011.121314","48.902",ip);
		ip = new InterpretedPattern(";?(\\d)",";*(inc,10);+(inc,1)","").compile();
		createProcess("th10s11s12131415br16.st17py18urg19m20up","th1s1s1337br0.st3py0urg4m3up",ip);
	}
	
	@Test
	public void testAssertion() {
		InterpretedPattern ip = new InterpretedPattern("a;?(.+)a","match","test");
		createProcess("testabba", "testabba", ip);
		createProcess("match", "abba", ip);
		ip = new InterpretedPattern("a;?(.+)a","match","L;test");
		createProcess("matchtest", "abbatest", ip);
		createProcess("abtestba", "abtestba", ip);
		
	}

	private void equals(String actual, String expected) {
		ec.checkThat(expected, CoreMatchers.equalTo(actual));
	}

	private void createProcess(String expected, String string, InterpretedPattern ip) {
		ArrayList<String> strings = new ArrayList<>();
		strings.add(string);
		currentExpected = expected;
		new CorrectionProcess(this, new ConsoleReceiver(), "JUNIT").process(strings, ip).run();
	}

	@Override
	public void collectFinished(String id, List<String> strings) {
		equals(currentExpected, strings.get(0));
	}
}
