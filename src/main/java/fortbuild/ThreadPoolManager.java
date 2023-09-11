package fortbuild;

import java.util.*;
import java.util.concurrent.*;

/**
 * Can be called anywhere to manage the ThreadPool such as performing a shutdown
 */
public class ThreadPoolManager
{
    public void shutdownExecutor(ExecutorService executor)
    {
        if(executor == null)
        {
            return;  // No need to shutdown if there is no executor
        }

        // Initiate Shutdown - no more task can be added in the executor
        executor.shutdown();
        
        // Wait for 30 seconds for tasks to finish, otherwise FORCE SHUTDOWN
        try
        {
            if(!executor.awaitTermination(30, TimeUnit.SECONDS))
            {
                System.out.println("Some tasks didn't finish in time. Forcing shutdown.");
                List<Runnable> unexecutedTasks = executor.shutdownNow();
                executor = null;
                handleUnexecutedTasks(unexecutedTasks);
            }
        }
        catch(InterruptedException ie)
        {
            System.out.println("Thread was interrupted while waiting for it to finish");
            executor.shutdownNow(); // Force shutdown if waiting was interrupted
        }
    }
    
    /**
     * Write/Handle what to do with unexecutedTasks
     */
    private void handleUnexecutedTasks(List<Runnable> unexecutedTasks)
    {
        System.out.println("Forced shutdown, now handling unexecuted tasks");
        for (Runnable task : unexecutedTasks)
        {   // Below code is just an example, which outputs all unexecutedTasks
            System.out.println("Task: " + task.toString() + " was not done.");
        }
    }
}
