package de.minetropolis.commandcorrectorutil;

import java.util.List;

public class Groups {
	public static List<Group> groups;

}

class Group {
	
	public int start = 0;
	public int end = 0;
	public int startOffset = 0;
	public int endOffset = 0;
	public boolean group = false;
	public boolean capturing = false;
	
	public Group(int start, int end, int startOffset, int endOffset, boolean group, boolean capturing) {
		this.start= start;
		this.end = end;
		this.group = group;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.capturing = capturing;
	}
	
	public Group next() {
		Group nextGroup = Groups.groups.get(Groups.groups.indexOf(this) + 1);
		if (nextGroup.capturing)
			return nextGroup;
		else
			return nextGroup.next();
	}
	
	public void addOffset(int offset, int position) {
		if (position > end + endOffset)
			return;
		startOffset += offset;
		endOffset += offset;
		if (Groups.groups.indexOf(this) < Groups.groups.size() - 1)
			Groups.groups.get(Groups.groups.indexOf(this) + 1).addOffset(offset, position);
	}
}