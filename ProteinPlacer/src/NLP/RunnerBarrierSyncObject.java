package NLP;

public class RunnerBarrierSyncObject {
	
	int numberOfRunners;

	public RunnerBarrierSyncObject() {
		this.numberOfRunners = 0;
	}
	
	public RunnerBarrierSyncObject(int numberOfRunners) {
		super();
		this.numberOfRunners = numberOfRunners;
	}
	
	public synchronized int getNumberOfRunners() {
		return numberOfRunners;
	}

	public synchronized void setNumberOfRunners(int numberOfRunners) {
		this.numberOfRunners = numberOfRunners;
	}
	
	public synchronized void incrementRunners(){
		numberOfRunners++;
	}
	
	public synchronized void decrementRunners(){
		numberOfRunners--;
	}
}
