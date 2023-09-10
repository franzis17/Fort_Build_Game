package fortbuild;

import java.util.*;
import java.util.concurrent.*;

public class TestApp
{
    // Represents corners of grid
    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_LEFT = 2;
    private static final int BOTTOM_RIGHT = 3;
    
    private int endX = 8;
    private int endY = 8;
    
    private ConcurrentHashMap<String, Wall> wallMap = new ConcurrentHashMap<>();
    private BlockingQueue<Wall> wallQueue = new ArrayBlockingQueue<>(10);
    
    public TestApp()
    {}
    
    /** Call one of the methods below to test a function */
    public void main()
    {
        // testConcurrentHashMap();
        // testScheduledThreadPool();
        // testRandomNumber();
        // testCorners();
        testRounding();
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
    
    public void testRandomNumber()
    {
        System.out.println("Testing Random Number with a range");

        // Define the range (inclusive of min and max)
        int min = 500;
        int max = 2000;

        // Generate a random integer within the specified range
        for(int i = 0; i < 4000; i++)  // Simulate 4000 random numbers to test
        {
            int randomNumber = ThreadLocalRandom.current().nextInt(min, max + 1);
            
            // Print the random number
            System.out.println("Random number between " + min + " and "
                + max + ": " + randomNumber);
            if(randomNumber < min)
            {
                System.out.println("ERROR: Below minimum");
                return;
            }
            if(randomNumber > max)
            {
                System.out.println("ERROR: Above maximum");
                return;
            }
        }
        
        System.out.println("Successfully completed without errors");
    }
    
    public void testCorners()
    {
        int[][] gridCorners = new int[4][2];  // 4 corners with 2 coords, x and y
        int[] topLeftCoord = {0, 0};
        int[] topRightCoord = {endX, 0};
        int[] bottomLeftCoord = {0, endY};
        int[] bottomRightCoord = {endX, endY};
        
        // Initialise the four corners of the grid to place the robot in
        gridCorners[TOP_LEFT] = topLeftCoord;
        gridCorners[TOP_RIGHT] = topRightCoord;
        gridCorners[BOTTOM_LEFT] = bottomLeftCoord;
        gridCorners[BOTTOM_RIGHT] = bottomRightCoord;
        
        // Pick a random corner from 4 corners
        try
        {
            for(int i = 0; i < 100; i++)
            {
                int randomCornerNum = MathUtility.getRandomNum(0, 3);
                int[] pickedCorner = gridCorners[randomCornerNum];
                System.out.println(
                    "Picked: x = " + pickedCorner[0] + ", y = " + pickedCorner[1]
                );
                Thread.sleep(1000);
            }
        }
        catch(InterruptedException ie)
        {
            System.out.println("Interrupted");
        }
    }
    
    public void testRounding()
    {
        System.out.println("Testing Rounding a number by ceiling function");
        double num = 7;
        double half = num / 2;
        System.out.println("Number = " + num + ", (Double) / 2 = " + half);
        int floorResult = (int)Math.floor(num / 2);
        int ceilResult = (int)Math.ceil(num / 2);
        System.out.println("Floor Result = " + floorResult);
        System.out.println("Ceil Result = " + ceilResult);
        
        int numInt = 7;
        System.out.println("Number = " + numInt + ", (int) / 2 = " + (numInt/2));
    }
}
