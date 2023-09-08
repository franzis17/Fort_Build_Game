package fortbuild;

import java.util.concurrent.*;

public class ThreadPoolManager
{
    public static void shutdownExecutor(ExecutorService executor)
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
                executor.shutdownNow();
            }
        }
        catch(InterruptedException ie)
        {
            System.out.println("Thread was interrupted while waiting for it to finish");
            executor.shutdownNow(); // Force shutdown if waiting was interrupted
        }
    }
}
