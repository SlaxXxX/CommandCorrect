package de.minetropolis.groups;

import de.minetropolis.newutil.Statics;

public class Autoconvert extends Group {

	public Autoconvert(String content) {
		super(content);
	}

	@Override
	public Group apply() {
		content = "(" + content.substring(2, content.length() - 2).replaceAll("\\(", "(?:") + ")";
		for (int i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '"' && !Statics.isEscaped(content, i))
				content = content.substring(0, i) + content.substring(i + 1);
		}
		return this;
	}

	@Override
	public GroupType getType() {
		return GroupType.AUTOCONVERT;
	}

}
