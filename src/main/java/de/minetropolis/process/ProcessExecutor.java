package de.minetropolis.process;

import java.util.List;

public interface ProcessExecutor {
	void collectFinished(String id, List<String> strings);
}