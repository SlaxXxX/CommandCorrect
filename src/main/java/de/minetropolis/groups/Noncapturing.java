package de.minetropolis.groups;

public class Noncapturing extends Group {

	public Noncapturing(String content) {
		super(content);
	}

	@Override
	public String apply() {
		return content.substring(2);
	}

	@Override
	public GroupType getType() {
		return GroupType.NONCAPTURING;
	}

}
