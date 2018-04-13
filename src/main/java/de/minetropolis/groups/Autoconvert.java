package de.minetropolis.groups;

public class Autoconvert extends Group {

	public Autoconvert(String content) {
		super(content);
	}

	@Override
	public String apply() {
		String applied = "(" + content.substring(2, content.length() - 2) + ")";
		int count = 0;
		for (int i = 0; i < applied.length(); i++) {
			if (applied.charAt(i) == '(') {
				if (count == 1) {
					applied = applied.substring(0, i + 1) + "?:" + applied.substring(i + 1);
					i += 2;
				}
				count++;
			}
			if (applied.charAt(i) == ')')
				count--;
		}
		return applied;
	}

	@Override
	public GroupType getType() {
		return GroupType.AUTOCONVERT;
	}

}
