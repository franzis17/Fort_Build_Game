package fortbuild;

import java.util.*;
import java.util.concurrent.*;

public class TestApp
{
    private ConcurrentHashMap<String, Wall> wallMap = new ConcurrentHashMap<>();
    private BlockingQueue<Wall> wallQueue = new ArrayBlockingQueue<>(10);
    
    public TestApp()
    {}
    
    public void main()
    {
        //testConcurrentHashMap();
        testScheduledThreadPool();
    }
    
    public void testConcurrentHashMap()
    {
        Wall wall1 = new Wall(1, 1);
        Wall wall2 = new Wall(1, 1);
        Wall wall3 = new Wall(1, 2);
        Wall wall4 = new Wall(2, 2);
        
        addWallToMap(wall1);
        addWallToMap(wall2);
        addWallToMap(wall3);
        addWallToMap(wall4);
        
        iterateThroughMap();
    }
    
    public void addWallToMap(Wall wall)
    {
        if(wallMap.containsKey(wall.getCoords()))
        {
            System.out.println("A wall in coords ("+wall.getCoords()+") already exists");
            return;
        }
        wallMap.put(wall.getCoords(), wall);
        System.out.println("Wall has been added");
    }
    
    public void iterateThroughMap()
    {
        System.out.println("Map contains:");
        wallMap.forEach((key, value) ->
        {
            System.out.println("Wall coord: " + key);
        });
    }
    
    public void testScheduledThreadPool()
    {
        ScheduledExecutorService wallBuilderService = Executors.newScheduledThreadPool(1);
        TimeSinceExecution timer = new TimeSinceExecution();
        
        // Add walls in queue
        for(int i = 0; i < 4; i++)
        {
            wallQueue.offer(new Wall(i, i+1));
        }
        
        wallBuilderService.scheduleAtFixedRate(() ->
        {
            System.out.println("Wall Builder...");
            Wall newWall = wallQueue.poll();
            
            System.out.println("Time since execution: " + timer.getElapsedTime());
        }, 0, 2, TimeUnit.SECONDS);
    }
}
