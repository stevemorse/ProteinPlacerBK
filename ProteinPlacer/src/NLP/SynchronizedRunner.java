package NLP;

import org.concordia.cs.common.ncbi.qblast.GetCommand;
import org.concordia.cs.common.ncbi.qblast.PutCommand;
import org.concordia.cs.common.ncbi.qblast.QBlastRequest;


/**
 * @author: Farzad
 * modified by: Steve Morse
 * @created: May 23, 2006
 */
public class SynchronizedRunner extends Thread
{
    public SynchronizedRunner(PutCommand put_command, GetCommand get_command, RunnerBarrierSyncObject runner_sync)
    {
        this.putCommand = put_command;
        this.getCommand = get_command;
        this.barrier = runner_sync;
        threadIdSpawnValue++;
        threadId = threadIdSpawnValue;
    }

    public synchronized void run()
    {
 synchronized(barrier){
 System.out.println("runner starts---number: " + threadId + " barrier value is: " + barrier.getNumberOfRunners());
 //}
    	barrier.incrementRunners();
 }//synch
        try
        {
            long time = System.currentTimeMillis() + 1000;

            while (time>System.currentTimeMillis())
                yield();
            
            QBlastRequest.runCommand(putCommand);

            int wait_time = putCommand.getWaitingTime() * 1000;

            if (wait_time == 0)
                wait_time = 3000;

            do
            {
                time = System.currentTimeMillis() + wait_time;

                while (time>System.currentTimeMillis())
                    yield();

                wait_time = 5000;
                
                getCommand.setRequestID(putCommand.getRequestID());
                
                try
                {
                	QBlastRequest.runCommand(getCommand);
                }
                catch (Exception e)
                {
                	System.out.println("An exception happened during connecting to ncbi site. Trying again... Execption is: ");
                	e.printStackTrace();
                	System.out.println("Command is: " + getCommand );
                	continue;
                }
            }
            while (!getCommand.getStatus().contains(GetCommand.STATUS_READY)) ;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        synchronized(barrier){
        barrier.decrementRunners();
//synchronized(barrier){
System.out.println("runner terminates---number: "  + threadId + " barrier value is: " + barrier.getNumberOfRunners());
}
    }

    public RunnerBarrierSyncObject getBarrier() {
		return barrier;
	}

	public void setBarrier(RunnerBarrierSyncObject barrier) {
		this.barrier = barrier;
	}

	public static int getThreadIdSpawnValue() {
		return threadIdSpawnValue;
	}

	public static void setThreadIdSpawnValue(int threadIdSpawnValue) {
		SynchronizedRunner.threadIdSpawnValue = threadIdSpawnValue;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public void setPutCommand(PutCommand putCommand) {
		this.putCommand = putCommand;
	}

	public void setGetCommand(GetCommand getCommand) {
		this.getCommand = getCommand;
	}

	public GetCommand getGetCommand()
    {
        return getCommand;
    }

    public PutCommand getPutCommand()
    {
        return putCommand;
    }

    private PutCommand putCommand;
    private GetCommand getCommand;
    private RunnerBarrierSyncObject barrier;
    private static int threadIdSpawnValue = 0;
    private int threadId;
    
}
