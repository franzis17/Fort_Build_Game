package fortbuild;

import java.util.concurrent.*;

public class WallCoordinator
{
    private BlockingQueue<Wall> wallQueue = new SynchronousQueue<>();
    
    public WallCoordinator()
    {}
    
    public void enqueueWall(Wall wall)
    {
        try
        {
            Thread.sleep(2000);
            wallQueue.put(wall);
        }
        catch(InterruptedException ie)
        {
            System.out.println("Interrupted enqueue to WallCoordinator");
        }
    }
    
    public Wall dequeueWall()
    {
        Wall wall = null;
        try
        {
            wall = wallQueue.take();
        }
        catch(InterruptedException ie)
        {
            System.out.println("Interrupted dequeue from WallCoordinator");
        }
        return wall;
    }
}
