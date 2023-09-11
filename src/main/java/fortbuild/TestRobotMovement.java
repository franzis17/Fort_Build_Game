package fortbuild;

import java.util.*;
import java.util.concurrent.*;

/**
 * Test Movement of Robot and waiting
 */
public class TestRobotMovement
{
    private ConcurrentHashMap<String, BlockingQueue<Robot>> robotMap = new ConcurrentHashMap<>();
    
    private Thread r1Thread = null;
    private Thread r2Thread = null;
    
    public void main()
    {
        // Simulate a 3x3 grid
        int gridWidth = 3;
        int gridHeight = 3;
        
        // 1. Initialise all grids to have a blockingQueue
        System.out.println("Initialising the grid...");
        for(int x = 0; x < gridWidth; x++)
        {
            for(int y = 0; y < gridHeight; y++)
            {
                // Each grid will have a blocking queue that can only fit 1 Robot.
                // BlockingQueue is used to implement waiting functionality if a robot
                // already exists within a grid.
                BlockingQueue<Robot> robotQueue = new ArrayBlockingQueue<>(1);
                
                // String is used as a key of each grid location, which uses a ',' (comma) to
                // separate the actual x and y coordinates.
                robotMap.put((x+","+y), robotQueue);
            }
        }
        
        // 2. Create the Robots
        System.out.println("Creating Robots...");
        Arena arena = new Arena(null);  // do not pass ui for now, just testing simple movement
        Robot robot1 = new Robot(1, 1000, 0, 0, arena);
        Robot robot2 = new Robot(2, 1000, 0, 1, arena);
        System.out.println("robot1 coordinate: " + robot1.getCoords());
        System.out.println("robot2 coordinate: " + robot2.getCoords());
        
        // 3. Add the robots in the map
        
        try
        {
            // Get the Queue of the grid location robot1 and robot2 is trying to go to
            // then place them in that grid's queue
            BlockingQueue<Robot> robotQueue = robotMap.get(robot1.getCoords());
            robotQueue.put(robot1);

            robotQueue = robotMap.get(robot2.getCoords());
            robotQueue.put(robot2);
        }
        catch(InterruptedException ie)
        {
            System.out.println("INTERRUPTED: Placing of robot1 and robot2 in the grid");
        } 
        
        // 4. start the movement
        moveRobot1(robot1, robot2);
        moveRobot2(robot1, robot2);
    }

    /** Simulate a robot to wait for another robot */
    public void moveRobot1(Robot robot1, Robot robot2)
    {
        Runnable robot1Task = () ->
        {
            try
            {
                Thread.sleep(5000);
                System.out.println("robot1 is moving...");
                
                // move robot1 to robot2's position and robot1 should wait
                robot1.moveTo(robot2.getX(), robot2.getY());
                BlockingQueue<Robot> gridQueue = robotMap.get(robot1.getCoords());
                System.out.println("Waiting for robot2 to move...");
                gridQueue.put(robot1);  // This should be BLOCKED / WAITING
                System.out.println("Successfully move robot1 to robot2's position");
            }
            catch(InterruptedException ie)
            {
                System.out.println("INTERRUPTED robot1");
            }
        };
        r1Thread = new Thread(robot1Task);
        r1Thread.start();
    }
    
    /** Simulate a robot to wait for another robot */
    public void moveRobot2(Robot robot1, Robot robot2)
    {
        Runnable robot2Task = () ->
        {
            try
            {
                Thread.sleep(10000);
                System.out.println("robot2 is moving...");
                
                // move robot2
                String oldPosition = robot2.getCoords();  // used for removing the old position
                String newPosition = 0 + "," + 2;
                robot2.moveTo(0, 2);                      // move robot2 to new position
                BlockingQueue<Robot> gridQueue = robotMap.get(newPosition);
                gridQueue.put(robot2);

                // remove robot2 from its old position so other robots can move to it
                gridQueue = robotMap.get(oldPosition);
                gridQueue.poll();  // remove the robot from the old position
            }
            catch(InterruptedException ie)
            {
                System.out.println("INTERRUPTED robot2");
            }
        };
        r2Thread = new Thread(robot2Task);
        r2Thread.start();
    }
    
    public void stopRobot1()
    {
        if(r1Thread == null)
            return;
        
        r1Thread.interrupt();
        r1Thread = null;
    }
    
    public void stopRobot2()
    {
        if(r2Thread == null)
            return;
        
        r2Thread.interrupt();
        r2Thread = null;
    }
}
