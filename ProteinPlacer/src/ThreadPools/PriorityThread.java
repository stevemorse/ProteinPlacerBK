package ThreadPools;

public abstract class PriorityThread extends Thread{
	private int jobPriority;

	public int getJobPriority() {
		return jobPriority;
	}

	public void setJobPriority(int jobPriority) {
		this.jobPriority = jobPriority;
	}	
}
