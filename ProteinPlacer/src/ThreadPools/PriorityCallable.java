package ThreadPools;

import java.util.concurrent.Callable;

public abstract class PriorityCallable implements Callable {
	private int priority;

	public PriorityCallable(int priority){
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}	
	
}//class

