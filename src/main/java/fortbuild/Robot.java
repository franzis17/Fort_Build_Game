package fortbuild;

public class Robot
{
    private Thread robotThread = null;

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    private int id;
    private int d;  // delay
    // Robot's location in the grid
    private int x;
    private int y;
    // Used to draw the updated location of the Robot and know where the centre is
    private Arena arena;

    // Centre location of the grid where the robot moves to
    private int xCentre;
    private int yCentre;
    
    public Robot(int id, int d, int x, int y, Arena arena)
    {
        if(delayIsInvalid(d))
        {
            throw new IllegalArgumentException("Delay must be between 500-2000, got: " + d);
        }
        
        this.id = id;
        this.d = d;
        this.x = x;
        this.y = y;
        this.arena = arena;
        this.xCentre = arena.getXCentre();
        this.yCentre = arena.getYCentre();
    }
    
    public int getId()
    {
        return id;
    }
    
    public int getDelay()
    {
        return d;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public String getName()
    {
        return ("Robot-" + id);
    }
    
    public String getCoords()
    {
        return (x+","+y);
    }
    
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public void setX(int x)
    {
        this.x = x;
    }
    
    public void setY(int y)
    {
        this.y = y;
    }
    

    public void moveLeft()
    {
        x--;
    }

    public void moveRight()
    {
        x++;
    }
    
    public void moveUp()
    {
        y--;
    }
    
    public void moveDown()
    {
        y++;
    }
    
    private void moveHorizontally()
    {
        if(inCitadel())
        {
            System.out.println("Game over!");
            // arena.gameOver();
        }
        // Move left or right towards the centre
        else if(x < xCentre)
        {
            moveRight();
        }
        else if(x > xCentre)
        {
            moveLeft();
        }
        // if the Robot is at the centre of x-axis
        // then the way towards the Citadel will only be up or down
        else if(x == xCentre)  
        {
            moveVertically();
        }
    }
    
    private void moveVertically()
    {
        if(inCitadel())
        {
            System.out.println("Game over!");
            // arena.gameOver();
        }
        // Move up or down towards the centre
        else if(y < yCentre)
        {
            moveDown();
        }
        else if(y > yCentre)
        {
            moveUp();
        }
        // if the Robot is at the centre of y-axis
        // then the way towards the Citadel will only be left or right
        else if(y == yCentre)
        {
            moveHorizontally();
        }
    }
    

    /**
     * Move randomly BUT towards the Citadel called by RobotGenerator after
     * it places it on the grid
     */
    public void startMoving()
    {
        Runnable moveTask = () ->
        {
            try
            {
                while(!Thread.interrupted())
                {
                    // Move depending on the delay time of the robot
                    Thread.sleep(d);
                    
                    String oldLocation = getCoords();
                    
                    // Move either horizontally or vertically towards the Citadel
                    int randomMovement = MathUtility.getRandomNum(0, 1);
                    if(randomMovement == HORIZONTAL)
                    {
                        moveHorizontally();
                    }
                    else if(randomMovement == VERTICAL)
                    {
                        moveVertically();
                    }

                    if(!inCitadel())
                    {
                        System.out.println("Robot-"+id+" moved from (" + oldLocation
                            + ") to ("+getCoords()+")");
                        arena.drawArena();
                    }
                }
            }
            catch(InterruptedException ie)
            {
                System.out.println("INTERRUPTED Robot("+id+") Movement");
            }
        };
        robotThread = new Thread(moveTask, (getName()+"-thread"));
        robotThread.start();
    }

    /**
     * This is collectively called in Arena to stop each Robot in the robotMap.
     * The 'collective' stop will initiate when user wishes to close the app window.
     */
    public void stop()
    {
        if(robotThread == null)
        {
            return;
        }
        
        robotThread.interrupt();
        robotThread = null;
    }
    

    // ** VALIDATIONS **
    
    
    private boolean delayIsInvalid(int d)
    {
        return (d < 500 && d > 2000);
    }
    
    private boolean inCitadel()
    {
        return (x == xCentre && y == yCentre);
    }
}
