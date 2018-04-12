import de.minetropolis.newutil.InterpretedPattern;

public class Test {
	String[] tests = {
		"Plain String: My name is Steve",
		"Dodge this: -> ;?(Test)",
		"Brackets: []  -  ;?([])  -  \\;?([])  -  \\\\;?([])  -  \\\\\\;?([])  -  \\\\\\\\\\\\;?([])",
		"This is an Autoconverter: ;>(\"another\"|\"test\")|(\"bra\"|\"heh\")<;",
		"Now look at all these braces: ;?(a(b)(c(d))e(f(g)(h)))",
		"Let's combine some stuff :) : ;>(\"another\"|\"test\")|(\"bra\"|\"heh\")<;  ++  ;?(a(b)(c(d))e(f(g)(h)))  ++  ;>(\"another\"|\"test\")|(\"bra\"|\"heh\")<;  ++  ;?(a(b)(c(d))e(f(g)(h)))",
		"How about a real world example?: ;?(?:\\/?)setblock ;?((?:~?-?\\d* *){3});?((?:minecraft\\:)?\\w+);?(?: *);?((?:\\d+|(?:\\w+=\\w+,?)+)?);?(?: *);?((?:\\w+)?);?(?: *);?((?:\\{.+\\})?)"
	};

	public static void main(String[] args) {
		new Test();
	}

	private Test() {
		for (int i = 0; i < tests.length; i++) {
			System.out.println(tests[i]);
			System.out.println(new InterpretedPattern(tests[i]).compile().pattern + "\n");
		}
	}
}
