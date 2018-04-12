package de.minetropolis.groups;

public class Noncapturing extends Group {

	public Noncapturing(String content) {
		super(content);
	}

	@Override
	public Group apply() {
		content = content.substring(2);
		return this;
	}

	@Override
	public GroupType getType() {
		return GroupType.NONCAPTURING;
	}

}
