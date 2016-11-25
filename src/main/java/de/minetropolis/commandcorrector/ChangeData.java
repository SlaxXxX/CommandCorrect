package de.minetropolis.commandcorrector;

import java.util.Collections;
import java.util.Map;

final class ChangeData {

	private final int amount;
	private final int blocksChanged;
	private final Map<String, Integer> changes;
	
	ChangeData(int amount, int changed, Map<String, Integer> changes) {
		this.amount = amount;
		this.blocksChanged = changed;
		this.changes = changes;
	}

	public int getAmount() {
		return amount;
	}

	public int getChanged() {
		return blocksChanged;
	}

	public Map<String, Integer> getChanges() {
		return Collections.unmodifiableMap(changes);
	}
	
	public int getChangeRulesApplied() {
		return changes.values().stream().mapToInt(i -> i).sum();
	}
}
