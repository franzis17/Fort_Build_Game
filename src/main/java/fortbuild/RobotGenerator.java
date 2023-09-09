package fortbuild;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private int endX = 8;
    private int endY = 8;
    
    private volatile Thread robotGenThread = null;
    
    // Grid coordinates of the 4 corners
    private int[][] gridCorners = new int[4][2];        // 4 corners with 2 coords, x and y
    private final int[] topLeftCoord = {0, 0};          // Top Left Corner at: x = 0, y = 0
    private final int[] topRightCoord = {endX, 0};
    private final int[] bottomLeftCoord = {0, endY};
    private final int[] bottomRightCoord = {endX, endY};
    
    private JFXArena arena;
    
    public RobotGenerator(JFXArena arena)
    {
        this.arena = arena;

        // Initialise the four corners of the grid to place the robot in
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
            try
            {
                int robotIdCounter = 1;  // only accessed by one thread
                while(!Thread.interrupted())
                {
                    int[] randomCorner = getRandomCorner();
                    
                    Robot newRobot = new Robot(
                        0,  // Put 0 for now, will change when there are no robots occupied
                        MathUtility.getRandomNum(500, 2000),  // Delay value between 500-2000
                        randomCorner[X],
                        randomCorner[Y]
                    );
                    
                    if(!arena.robotIsOccupied(newRobot))
                    {
                        System.out.println("Corner is not occupied, placing the robot");
                        System.out.println("Robot Coords = " + newRobot.getCoords());
                        newRobot.setId(robotIdCounter++);
                        arena.addRobot(newRobot);
                    }
                    Thread.sleep(1500);
                }
            }
            catch(InterruptedException ie)
            {
                System.out.println("Interrupted Robot Generation");
            }
        };
        robotGenThread = new Thread(robotGenTask);
        robotGenThread.start();
    }
    
    public void stop()
    {
        if(robotGenThread == null)
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
