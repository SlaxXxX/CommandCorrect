package de.minetropolis.groups;

public class Escaped extends Group {

	public Escaped(String content) {
		super(content);
	}

	@Override
	public Group apply() {
		String escapable = "\\/()[]{}?*+.$^|";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			if (escapable.contains("" + content.charAt(i)))
				sb.append("\\");
			sb.append(content.charAt(i));
		}
		content = sb.toString();
		return this;
	}

	@Override
	public GroupType getType() {
		return GroupType.ESCAPED;
	}

}
