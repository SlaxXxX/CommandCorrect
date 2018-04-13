package de.minetropolis.groups;

import de.minetropolis.newutil.Statics;

public class Autoconvert extends Group {

	public Autoconvert(String content) {
		super(content);
	}

	@Override
	public String apply() {
		String applied = "(" + content.substring(2, content.length() - 2) + ")";//.replaceAll("\\(", "(?:") + ")";
		for (int i = 0; i < applied.length(); i++) {
			if (applied.charAt(i) == '"' && !Statics.isEscaped(applied, i))
				applied = applied.substring(0, i) + applied.substring(i + 1);
		}
		return applied;
	}

	@Override
	public GroupType getType() {
		return GroupType.AUTOCONVERT;
	}

}
