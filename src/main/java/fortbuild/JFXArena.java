package fortbuild;

import javafx.application.*;
import javafx.scene.canvas.*;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A JavaFX GUI element that displays a grid on which you can draw images, text and lines.
 */
public class JFXArena extends Pane
{
    private final int WALL_LIMIT = 10;  // Max amount of walls that can be in the screen
    
    // Represents an image to draw, retrieved as a project resource.
    private static final String ROBOT_FILE = "robot.png";
    private static final String WALL_FILE = "wall-full.png";
    private static final String WALL_BROKEN_FILE = "wall-broken.png";
    private Image robotImg;
    private Image wallImg;
    private Image brokenImg;
    
    // The following values are arbitrary, and you may need to modify them according to the 
    // requirements of your application.
    private int gridWidth = 9;
    private int gridHeight = 9;

    private double gridSquareSize; // Auto-calculated
    private Canvas canvas; // Used to provide a 'drawing surface'.

    private List<ArenaListener> listeners = null;
    
    // Limited amount of Robots that can be drawn in the UI
    private final int ROBOT_LIMIT = gridWidth * gridHeight;
    
    private BlockingQueue<Wall> wallQueue = new ArrayBlockingQueue<>(WALL_LIMIT);
    private ConcurrentHashMap<String, Wall> wallMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Robot> robotMap = new ConcurrentHashMap<>();
    
    private ScheduledExecutorService wallBuildScheduler;
    private volatile Thread wallBuilderThread = null;
    
    /**
     * Creates a new arena object, loading the robot image and initialising a drawing surface.
     */
    public JFXArena()
    {
        robotImg = openImgFile(ROBOT_FILE);
        wallImg = openImgFile(WALL_FILE);
        brokenImg = openImgFile(WALL_BROKEN_FILE);
        
        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
        
        // Start background tasks
        startWallBuilder();
    }

    /**
     * Here's how (in JavaFX) you get an Image object from an image file that's part of the
     * project's "resources". If you need multiple different images,you can modify this 
     * code accordingly.
     */
    private Image openImgFile(String imgFilename)
    {
        Image img = null;
        
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(imgFilename))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + imgFilename);
            }
            img = new Image(is);
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + imgFilename, e);
        }
        
        return img;
    }
    
    // /**
    //  * Moves a robot image to a new grid position. This is highly rudimentary, as you will need
    //  * many different robots in practice. This method currently just serves as a demonstration.
    //  */
    // public void setRobotPosition(double x, double y)
    // {
    //     robotX = x;
    //     robotY = y;
    //     requestLayout();
    // }
    
    /**
     * Adds a callback for when the user clicks on a grid square within the arena. The callback 
     * (of type ArenaListener) receives the grid (x,y) coordinates as parameters to the 
     * 'squareClicked()' method.
     */
    public void addListener(ArenaListener newListener)
    {
        if(listeners == null)
        {
            listeners = new LinkedList<>();
            setOnMouseClicked(event ->
            {
                int gridX = (int)(event.getX() / gridSquareSize);
                int gridY = (int)(event.getY() / gridSquareSize);
                
                if(gridX < gridWidth && gridY < gridHeight)
                {
                    for(ArenaListener listener : listeners)
                    {   
                        listener.squareClicked(gridX, gridY);
                    }
                }
            });
        }
        listeners.add(newListener);  // maybe use blockingqueue instead of linkedlist to build walls
    }
    
    /** Called by RobotGenerator Thread whenever it has generated a new robot */
    public void addRobot(Robot newRobot)
    {
        if(newRobot == null)
        {
            return;
        }
        if(!robotIsOccupied(newRobot))
        {
            System.out.println("Putting Robot in robotMap");
            robotMap.put(newRobot.getCoords(), newRobot);  // Uses ConcurrentHashMap
            drawArena();
        }
    }
    
    public boolean robotIsOccupied(Robot newRobot)
    {
        if(robotMap.containsKey(newRobot.getCoords()))
        {
            System.out.println("The grid " + newRobot.getCoords()
                + " is already occupied with Robots.");
            return true;
        }
        return false;
    }
    
    
    public void enqueueWall(Wall newWall)
    {
        // If the map is full, do not add anymore
        if(wallMap.size() >= WALL_LIMIT)
        {
            System.out.println("The Arena can only have 10 walls");
            System.out.println("Amount of Walls in Map = " + wallMap.size());
            return;
        }
        // Do not add if the queue is full
        boolean status = wallQueue.offer(newWall);
        if(status == false)
        {
            System.out.println("No space");
            System.out.println(wallQueue.size());
            return;
        }
    }

    private void startWallBuilder()
    {
        wallBuildScheduler = Executors.newScheduledThreadPool(1);
        
        // Builds a wall every 2 seconds
        wallBuildScheduler.scheduleAtFixedRate(() ->
        {
            System.out.println("Building a wall...");
            
            Wall newWall = wallQueue.poll();
            if(newWall == null)
            {   // ignore
            }
            else if(wallMap.size() >= WALL_LIMIT)
            {
                System.out.println("Cannot do wall command, The Arena can only have 10 walls");
                System.out.println("Amount of Walls in Map = " + wallMap.size());
                // if the wall is already full, ignore all build commands in the queue
                wallQueue.clear();
            }
            else if(!wallIsOccupied(newWall))
            {   
                System.out.println("Putting wall in hashmap");
                wallMap.put(newWall.getCoords(), newWall);
                drawArena();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }
    
    /** Called when the user closes the window */
    public void endWallBuilder()
    {
        if(wallBuildScheduler == null)
        {
            return;  // Thread does not exist so it cannot be interrupted
        }
        ThreadPoolManager.shutdownExecutor(wallBuildScheduler);
    }
    
    /** Checks the map if the map already contains a wall in the coordinates */
    private boolean wallIsOccupied(Wall newWall)
    {
        if(wallMap.containsKey(newWall.getCoords()))
        {
            System.out.println("A wall already exist in "
                        + "("+newWall.getCoords()+")");
            return true;
        }
        return false;
    }

    /**
     * This method is called in order to redraw the screen, either because the user is manipulating 
     * the window, OR because you've called 'requestLayout()'.
     *
     * You will need to modify the last part of this method; specifically the sequence of calls to
     * the other 'draw...()' methods. You shouldn't need to modify anything else about it.
     */
    @Override
    public void layoutChildren()
    {
        super.layoutChildren(); 
        GraphicsContext gfx = canvas.getGraphicsContext2D();
        gfx.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
        
        // First, calculate how big each grid cell should be, in pixels. (We do need to do this
        // every time we repaint the arena, because the size can change.)
        gridSquareSize = Math.min(
            getWidth() / (double) gridWidth,
            getHeight() / (double) gridHeight);
            
        double arenaPixelWidth = gridWidth * gridSquareSize;
        double arenaPixelHeight = gridHeight * gridSquareSize;
            
        // Draw the arena grid lines. This may help for debugging purposes, and just generally
        // to see what's going on.
        gfx.setStroke(Color.DARKGREY);
        gfx.strokeRect(0.0, 0.0, arenaPixelWidth - 1.0, arenaPixelHeight - 1.0); // Outer edge

        for(int gridX = 1; gridX < gridWidth; gridX++) // Internal vertical grid lines
        {
            double x = (double) gridX * gridSquareSize;
            gfx.strokeLine(x, 0.0, x, arenaPixelHeight);
        }
        
        for(int gridY = 1; gridY < gridHeight; gridY++) // Internal horizontal grid lines
        {
            double y = (double) gridY * gridSquareSize;
            gfx.strokeLine(0.0, y, arenaPixelWidth, y);
        }

        // -- original code ---
        // Invoke helper methods to draw things at the current location.
        // ** You will need to adapt this to the requirements of your application. **
        //drawImage(gfx, robot1, robotX, robotY);
        //drawLabel(gfx, "Robot Name", robotX, robotY);
        
        // ** Uses two executors to draw each object types all at once **
        
        // Draw the robots
        ExecutorService drawRobotsExecutor = Executors.newFixedThreadPool(ROBOT_LIMIT);
        robotMap.forEach((robotCoord, robot) ->
        {
            drawRobotsExecutor.submit(() ->
            {
                Platform.runLater(() ->
                {
                    drawImage(gfx, robotImg, robot.getX(), robot.getY());
                    drawLabel(gfx, robot.getName(), robot.getX(), robot.getY());
                });
            });
        });

        // Draw the walls
        ExecutorService drawWallsExecutor = Executors.newFixedThreadPool(WALL_LIMIT*2);
        wallMap.forEach((wallCoord, wall) ->
        {
            drawWallsExecutor.submit(() ->
            {
                Platform.runLater(() ->
                {
                    drawImage(gfx, wallImg, wall.getX(), wall.getY());
                });
            });
        });
        
        // After drawing all objects, must shutdown the executors
        ThreadPoolManager.shutdownExecutor(drawRobotsExecutor);
        ThreadPoolManager.shutdownExecutor(drawWallsExecutor);
    }
    
    /** 
     * Draw an image in a specific grid location. *Only* call this from within layoutChildren(). 
     *
     * Note that the grid location can be fractional, so that (for instance), you can draw an image 
     * at location (3.5,4), and it will appear on the boundary between grid cells (3,4) and (4,4).
     *     
     * You shouldn't need to modify this method.
     */
    private void drawImage(GraphicsContext gfx, Image image, double gridX, double gridY)
    {
        // Get the pixel coordinates representing the centre of where the image is to be drawn.
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;
        
        // We also need to know how "big" to make the image. The image file has a natural width 
        // and height, but that's not necessarily the size we want to draw it on the screen. We 
        // do, however, want to preserve its aspect ratio.
        double fullSizePixelWidth = robotImg.getWidth();
        double fullSizePixelHeight = robotImg.getHeight();
        
        double displayedPixelWidth, displayedPixelHeight;
        if(fullSizePixelWidth > fullSizePixelHeight)
        {
            // Here, the image is wider than it is high, so we'll display it such that it's as 
            // wide as a full grid cell, and the height will be set to preserve the aspect 
            // ratio.
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        }
        else
        {
            // Otherwise, it's the other way around -- full height, and width is set to 
            // preserve the aspect ratio.
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        // Actually put the image on the screen.
        gfx.drawImage(image,
            x - displayedPixelWidth / 2.0,  // Top-left pixel coordinates.
            y - displayedPixelHeight / 2.0, 
            displayedPixelWidth,              // Size of displayed image.
            displayedPixelHeight);
    }
    
    
    /**
     * Displays a string of text underneath a specific grid location. *Only* call this from within 
     * layoutChildren(). 
     *     
     * You shouldn't need to modify this method.
     */
    private void drawLabel(GraphicsContext gfx, String label, double gridX, double gridY)
    {
        gfx.setTextAlign(TextAlignment.CENTER);
        gfx.setTextBaseline(VPos.TOP);
        gfx.setStroke(Color.BLUE);
        gfx.strokeText(label, (gridX + 0.5) * gridSquareSize, (gridY + 1.0) * gridSquareSize);
    }
    
    /** 
     * Draws a (slightly clipped) line between two grid coordinates.
     *     
     * You shouldn't need to modify this method.
     */
    private void drawLine(GraphicsContext gfx, double gridX1, double gridY1, 
                                               double gridX2, double gridY2)
    {
        gfx.setStroke(Color.RED);
        
        // Recalculate the starting coordinate to be one unit closer to the destination, so that it
        // doesn't overlap with any image appearing in the starting grid cell.
        final double radius = 0.5;
        double angle = Math.atan2(gridY2 - gridY1, gridX2 - gridX1);
        double clippedGridX1 = gridX1 + Math.cos(angle) * radius;
        double clippedGridY1 = gridY1 + Math.sin(angle) * radius;
        
        gfx.strokeLine((clippedGridX1 + 0.5) * gridSquareSize, 
                       (clippedGridY1 + 0.5) * gridSquareSize, 
                       (gridX2 + 0.5) * gridSquareSize, 
                       (gridY2 + 0.5) * gridSquareSize);
    }
    
    private void drawArena()
    {
        Platform.runLater(() ->
        {
            requestLayout();
        });
    }
}
