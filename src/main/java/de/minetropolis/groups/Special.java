package de.minetropolis.groups;

public class Special extends Group {

	public Special(String content) {
		super(content);
	}

	@Override
	public Group apply() {
		content = content.substring(2);
		return this;
	}

	@Override
	public GroupType getType() {
		return GroupType.SPECIAL;
	}

}
