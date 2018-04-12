package de.minetropolis.groups;

public class Normal extends Group {

	public Normal(String content) {
		super(content);
	}

	@Override
	public Group apply() {
		return this;
	}

	@Override
	public GroupType getType() {
		return GroupType.NORMAL;
	}

}
