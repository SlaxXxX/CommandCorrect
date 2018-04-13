package de.minetropolis.groups;

public class Special extends Group {

	public Special(String content) {
		super(content);
	}

	@Override
	public String apply() {
		return content.substring(2);
	}

	@Override
	public GroupType getType() {
		return GroupType.SPECIAL;
	}

}
