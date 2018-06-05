import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import de.minetropolis.groups.*;
import de.minetropolis.newutil.InterpretedPattern;
import de.minetropolis.newutil.Statics;
import junit.framework.Assert;

public class UnitTest extends Assert {

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
		equals(expected[0],ip.pattern);
		for (int i = 1; i<expected.length;i++)
			equals(expected[i],ip.groups.get(i-1).getContent());
		
		ip = new InterpretedPattern(";?(a(b)c(d(e)f(g)h)i)").compile();
		String[] expected1 = {
			"(a(b)c(d(e)f(g)h)i)",
			"(a(b)c(d(e)f(g)h)i)",
			"(b)",
			"(d(e)f(g)h)",
			"(e)",
			"(g)"
		};
		equals(expected1[0],ip.pattern);
		for (int i = 1; i<expected1.length;i++)
			equals(expected1[i],ip.groups.get(i-1).getContent());
	}
	
	@Test
	public void testStatics() {
		InterpretedPattern ip = new InterpretedPattern(";?([\\[,]{1});?(?: *);>(g|gamemode)<;;?(?: *)=;?(?: *);>(0|s|survival)|(1|c|creative)|(2|a|adventure)|(3|sp|spectator)<;;?(?: *);?([\\],]{1})"
			,";:(1);:(2)=;:(3);:(4)", "").compile();
		equals("@a[gamemode=survival]",Statics.changeCommand(ip, "@a[g=s]",null));
		equals("@s[gamemode=spectator]",Statics.changeCommand(ip, "@s[gamemode=sp]",null));
		equals("@a[gamemode=creative]",Statics.changeCommand(ip, "@a[g=1]",null));
		equals("@r[gamemode=creative]",Statics.changeCommand(ip, "@r[gamemode=creative]",null));
		
		equals("[A]",Statics.changeCommand(new InterpretedPattern(";>(\\((\\w)\\)|\\[\\2\\])<;",";:(1)", "").compile(), "(A)",null));
		equals("\\Test\\",Statics.changeCommand(new InterpretedPattern(";>(Test|\\\\Test\\\\)<;",";:(1)", "").compile(), "Test",null));
	}
	
	private void equals(String actual, String expected) {
		ec.checkThat(expected, CoreMatchers.equalTo(actual));
	}
}
