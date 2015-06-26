package cmabreu.sagitarii.misc.json;

import java.util.List;

public class NodeTasks {
	private String nodeId;
	private String cpuLoad;
	private String freeMemory;
	private String totalMemory;
	private List<NodeTask> data;
	
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public List<NodeTask> getData() {
		return data;
	}
	
	public void setData(List<NodeTask> data) {
		this.data = data;
	}

	public double getCpuLoad() {
		return Double.parseDouble( cpuLoad );
	}

	public void setCpuLoad(String cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public long getFreeMemory() {
		return Long.parseLong( freeMemory );
	}

	public void setFreeMemory(String freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getTotalMemory() {
		return Long.parseLong( totalMemory );
	}

	public void setTotalMemory(String totalMemory) {
		this.totalMemory = totalMemory;
	}
	
}