package seleniumBlast;

import NLP.RunnerBarrierSyncObject;

/**
 * @author: Farzad
 * @created: May 23, 2006
 */
public class SeleniumRunner extends Thread
{
    public SeleniumRunner(SeleniumPutCommand put_command, SeleniumProteinGetCommand get_command, RunnerBarrierSyncObject runnerBarrier)
    {
        this.putCommand = put_command;
        this.getCommand = get_command;
    }

	public synchronized void run()
    {
        try
        {
            long time = System.currentTimeMillis() + 1000;

            while (time>System.currentTimeMillis())
                yield();
            
            SeleniumQBlastRequest.runCommand(putCommand);

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
                	SeleniumQBlastRequest.runCommand(getCommand);
                }
                catch (Exception e)
                {
                	System.out.println("An exception happened during connecting to ncbi site. Trying again... Execption is: ");
                	e.printStackTrace();
                	System.out.println("Command is: " + getCommand );
                	continue;
                }
            }
            while (!getCommand.getStatus().contains(SeleniumGetCommand.STATUS_READY)) ;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public SeleniumGetCommand getGetCommand()
    {
        return getCommand;
    }

    public SeleniumPutCommand getPutCommand()
    {
        return putCommand;
    }

    SeleniumPutCommand putCommand;
    SeleniumGetCommand getCommand;
}
