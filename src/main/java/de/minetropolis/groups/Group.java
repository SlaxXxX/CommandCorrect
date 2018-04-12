package de.minetropolis.groups;

public abstract class Group {
	protected String content;
	private int start, end;

	public Group(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Group setEnd(int end) {
		this.end = end;
		return this;
	}

	public abstract Group apply();

	public abstract GroupType getType();

	public String toString() {
		return "\"" + content + "\"";
	}
}
