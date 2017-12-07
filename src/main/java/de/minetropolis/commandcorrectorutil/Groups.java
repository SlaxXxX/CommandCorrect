package de.minetropolis.commandcorrectorutil;

import java.util.List;

public class Groups {
	public static List<Group> groups;

}

class Group {
	
	public int start = 0;
	public int end = 0;
	public int offset = 0;
	public boolean group = false;
	public boolean capturing = false;
	
	public Group() {
	}
	
	public Group(int start, int end, int offset, boolean group, boolean capturing) {
		this.start= start;
		this.end = end;
		this.group = group;
		this.offset = offset;
		this.capturing = capturing;
	}
	
	public Group next() {
		Group nextGroup = Groups.groups.get(Groups.groups.indexOf(this) + 1);
		if (nextGroup.capturing)
			return nextGroup;
		else
			return nextGroup.next();
	};
}