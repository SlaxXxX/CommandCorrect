package de.minetropolis.commandcorrectorutil;

import java.util.ArrayList;
import java.util.List;

public class InterpretedPattern {
	public List<Group> groups = new ArrayList<>();
	public String pattern,target,assertion;

	public InterpretedPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public InterpretedPattern(String pattern, String target, String assertion) {
		this.pattern = pattern;
		this.target = target;
		this.assertion = assertion;
	}
	
	public InterpretedPattern fill(String target, String assertion) {
		this.target = target;
		this.assertion = assertion;
		return this;
	}

}

class Group {

	InterpretedPattern ip;
	public int start = 0;
	public int end = 0;
	public int startOffset = 0;
	public int endOffset = 0;
	public boolean group = false;
	public boolean capturing = false;

	public Group(InterpretedPattern ip, int start, int end, int startOffset, int endOffset, boolean group, boolean capturing) {
		this.ip = ip;
		this.start = start;
		this.end = end;
		this.group = group;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.capturing = capturing;
	}

	public Group next() {
		if (ip.groups.indexOf(this) >= ip.groups.size() - 1)
			return this;
		Group nextGroup = ip.groups.get(ip.groups.indexOf(this) + 1);
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
		if (ip.groups.indexOf(this) < ip.groups.size() - 1)
			ip.groups.get(ip.groups.indexOf(this) + 1).addOffset(offset, position);
	}
}