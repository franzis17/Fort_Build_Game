package fortbuild;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Producer of Robots and adds it to the ConcurrentHashMap of Robots in Arena.
 * Once it finds an unoccupied random corner for the robot to place, it initiates
 * the robot to move.
 */
public class RobotGenerator
{
    // Index number of the 4 corners in the 2-D Array of Integers
    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_LEFT = 2;
    private static final int BOTTOM_RIGHT = 3;
    
    private static final int X = 0;
    private static final int Y = 1;
    
    // To know what the end of the grid is (could be changed later to be dynamic)
    private int endX;
    private int endY;
    // Needed by Robots to know where the centre so it can move there
    private int xCentre;
    private int yCentre;
    
    private Thread robotGenThread = null;  // only main app thread accesses this
    
    // Grid coordinates of the 4 corners
    private int[][] gridCorners = new int[4][2];        // 4 corners with 2 coords, x and y
    private int[] topLeftCoord;          
    private int[] topRightCoord;
    private int[] bottomLeftCoord;
    private int[] bottomRightCoord;
    
    private Arena arena;
    
    public RobotGenerator(Arena arena)
    {
        this.arena = arena;
        endX = arena.getGridWidth() - 1;
        endY = arena.getGridHeight() - 1;
        xCentre = arena.getXCentre();
        yCentre = arena.getYCentre();

        // Initialise the four corners of the grid to place the robot in
        topLeftCoord = new int[] { 0, 0 };
        topRightCoord = new int[] { endX, 0 };
        bottomLeftCoord = new int[] { 0, endY };
        bottomRightCoord = new int[] { endX, endY };
        
        gridCorners[TOP_LEFT] = topLeftCoord;          // gridCorners[0] = top-left coords
        gridCorners[TOP_RIGHT] = topRightCoord;        // gridCorners[1] = top-right coords
        gridCorners[BOTTOM_LEFT] = bottomLeftCoord;    // gridCorners[2] = bottom-left coords
        gridCorners[BOTTOM_RIGHT] = bottomRightCoord;  // gridCorners[3] = bottom-right coords
    }
    
    /** Randomly spawn a robot every 1.5 seconds */
    public void start()
    {
        if(robotGenThread != null)
        {
            throw new IllegalStateException("Tried to create a robot generator but one "
                + "already exists.");
        }
        
        Runnable robotGenTask = () ->
        {
            int robotIdCounter = 1;  // only accessed by one thread

            while(true)
            {
                try
                {
                    // Wait for 1500ms to place the robot
                    Thread.sleep(1500);
                    
                    // Pick a random corner location
                    int[] randomCorner = getRandomCorner();
                    
                    // Create a robot with the coordinate location of the random corner
                    Robot newRobot = new Robot(
                        0,  // id is set below once a Robot is placed in an unoccupied position
                        MathUtility.getRandomNum(500, 2000),  // Delay value between 500-2000
                        randomCorner[X],
                        randomCorner[Y],
                        arena
                    );

                    if(!arena.robotIsOccupied(newRobot))
                    {
                        // Place robot in arena , then start robot movement
                        System.out.println("Placed robot-"+robotIdCounter+ 
                            " at ("+newRobot.getCoords()+")");
                        newRobot.setId(robotIdCounter++);
                        arena.addRobot(newRobot);
                        newRobot.startMoving();
                    }
                }
                catch(InterruptedException ie)
                {
                    System.out.println("Interrupted Robot Generation");
                    break;  // Only exit out of the loop when interrupted(stopped)
                }
                catch(IllegalStateException ise)
                {
                    System.out.println(">>> ERROR: " + ise.getMessage());
                }
                catch(IllegalArgumentException iae)
                {
                    System.out.println(">>> ERROR: " + iae.getMessage());
                }
            }
        };
        robotGenThread = new Thread(robotGenTask);
        robotGenThread.start();
    }
    
    /** Called by main app thread when a user clicks on close window */
    public void stop()
    {
        if(robotGenThread == null)  // Sanity Check
        {
            throw new IllegalStateException("Tried to stop robot generator but there"
                + " is none.");
        }
        robotGenThread.interrupt();
        robotGenThread = null;
    }
    
    /** Randomly pick 1 out of 4 corners and return the coordinates of the picked corner */
    private int[] getRandomCorner()
    {
        int randomCornerNum = MathUtility.getRandomNum(0, 3);
        return gridCorners[randomCornerNum];
    }
}
