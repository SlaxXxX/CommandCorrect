package de.minetropolis.groups;

public abstract class Group {
	protected String content;

	public Group(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public abstract String apply();

	public abstract GroupType getType();

	public String toString() {
		return "\"" + content + "\"";
	}
}
