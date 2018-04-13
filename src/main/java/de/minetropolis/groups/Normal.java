package de.minetropolis.groups;

public class Normal extends Group {

	public Normal(String content) {
		super(content);
	}

	@Override
	public String apply() {
		return content;
	}

	@Override
	public GroupType getType() {
		return GroupType.NORMAL;
	}

}
