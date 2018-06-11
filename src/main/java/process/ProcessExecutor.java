package process;

import java.util.List;

public interface ProcessExecutor {
	public void collectFinished(String id, List<String> strings);
}
