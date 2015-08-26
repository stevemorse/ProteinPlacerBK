package ThreadPools;

public abstract class PriorityRunnable implements Runnable{
	private int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}	
	
}//class
