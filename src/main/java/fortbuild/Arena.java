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
public class Arena extends Pane
{
    private Thread wallBuilderThread = null;  // only the main app thread accesses this
    private ThreadPoolManager tpManager = new ThreadPoolManager();  // shutsdown executors
    
    private final int wallLimit = 10;  // Max amount of walls that can be in the screen
    
    // Represents an image to draw, retrieved as a project resource.
    private static final String ROBOT_FILE = "robot.png";
    private Image robotImg;
    private static final String WALL_FILE = "wall-full.png";
    private Image wallImg;
    // private static final String WALL_BROKEN_FILE = "wall-broken.png";
    // private Image brokenImg;
    
    // The following values are arbitrary, and you may need to modify them according to the 
    // requirements of your application.
    private int gridWidth;   // x
    private int gridHeight;  // y
    
    // Coordinate location of the centre/citadel (integer division rounds down)
    private int xCentre;
    private int yCentre;

    private double gridSquareSize; // Auto-calculated
    private Canvas canvas; // Used to provide a 'drawing surface'.

    private List<ArenaListener> listeners = null;
    
    // Limited amount of Robots that can be drawn in the UI
    private int robotLimit;
    
    private BlockingQueue<Wall> wallQueue = new ArrayBlockingQueue<>(wallLimit);
    private ConcurrentHashMap<String, Wall> wallMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Robot> robotMap = new ConcurrentHashMap<>();
    
    private UserInterface ui;
    
    /**
     * Creates a new arena object, loading the robot image and initialising a drawing surface.
     */
    public Arena(UserInterface ui)
    {
        robotImg = openImgFile(ROBOT_FILE);
        wallImg = openImgFile(WALL_FILE);
        // brokenImg = openImgFile(WALL_BROKEN_FILE);
        
        // may change to be dynamic (specified to be 9x9 in the requirements)
        gridWidth = 9;
        gridHeight = 9;
        robotLimit = gridWidth * gridHeight;
        xCentre = gridWidth / 2;
        yCentre = gridHeight / 2;
        
        this.ui = ui;
        
        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
    }
    
    public int getGridWidth()
    {
        return gridWidth;
    }
    
    public int getGridHeight()
    {
        return gridHeight;
    }
    
    public int getXCentre()
    {
        return xCentre;
    }
    
    public int getYCentre()
    {
        return yCentre;
    }

    /**
     * Here's how (in JavaFX) you get an Image object from an image file that's part of the
     * project's "resources". If you need multiple different images,you can modify this 
     * code accordingly.
     */
    private Image openImgFile(String imgFilename)
    {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(imgFilename))
        {
            if(is == null)
            {
                throw new AssertionError("Cannot find image file " + imgFilename);
            }
            Image img = new Image(is);
            return img;
        }
        catch(IOException e)
        {
            throw new AssertionError("Cannot load image file " + imgFilename, e);
        }
    }
    
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
    
    
    // ** ROBOT METHODS **
    
    
    /** Called by RobotGenerator whenever it has generated a new robot */
    public void addRobot(Robot newRobot)
    {
        if(newRobot == null)
        {
            return;
        }
        
        robotMap.put(newRobot.getCoords(), newRobot);  // Uses ConcurrentHashMap
        drawArena();
    }
    
    /** Called by RobotGenerator to check if there is a robot */
    public boolean robotIsOccupied(Robot newRobot)
    {
        if(robotMap.containsKey(newRobot.getCoords()))
        {
            return true;
        }
        return false;
    }
    
    /** 
     * Gets called when the user wishes to close the app window.
     */
    public void stopAllRobots()
    {
        robotMap.forEach((robotCoords, robot) ->
        {
            robot.stop();
        });
    }
    
    
    // ** WALL BUILDING **
    
    
    /** 
     * Adds wall to wallQueue(BlockingQueue) and then wall builder thread takes it out
     * to put it in the Arena's ConcurrentHashMap of Walls, which then gets drawn
     */
    public void enqueueWall(Wall newWall)
    {
        if(wallMap.size() >= wallLimit)  // If walls are full
        {
            System.out.println(">>> INVALID: The Arena can only have 10 walls. " +
                "Amount of Walls in Map = " + wallMap.size());
        }
        else
        {
            // Do not add if the queue is full
            boolean status = wallQueue.offer(newWall);
            if(status == true)  // if the wall was successfully inserted
            {
                ui.setWallQueueNum(wallQueue.size());
            }
        }
    }
    
    /** Builds a wall every 2 seconds. If occupied, quickly find next wall to build. */
    public void startWallBuilder()
    {
        Runnable wallBuilderTask = () ->
        {
            while(true)
            {
                try
                {
                    // Wait for next wall building command
                    Wall newWall = wallQueue.take();

                    // wait for 2 seconds to build the wall
                    Thread.sleep(2000);
                    
                    // If there is already 10 walls ("more than" symbol is put for safety check)
                    if(wallMap.size() >= wallLimit)
                    {
                        System.out.println(">>>INVALID: " +
                            "Cannot do wall command, The Arena can only have 10 walls\n" +
                            "Amount of Walls in Map = " + wallMap.size());
                        // clear all build commands so it processes the user's
                        // latest command when a wall gets destroyed
                        wallQueue.clear();
                        return;
                    }
                    
                    // If a wall is occupied, loop to find a wall that is not occupied so
                    // there is no delay when building the next wall.
                    while(wallIsOccupied(newWall))
                    {
                        System.out.println("Wall is occupied, quickly finding next wall");
                        newWall = wallQueue.poll();  // do not wait here
                        
                        if(newWall == null)
                        {
                            return;  // wait at the start if there are no more build commands
                        }
                    }
                    
                    System.out.println("Wall put in at ("+ newWall.getCoords()+")");
                    ui.setLog("Wall created");
                    wallMap.put(newWall.getCoords(), newWall);
                    drawArena();  // calls GUI Thread
                }
                catch(InterruptedException ie)
                {
                    System.out.println("INTERRUPTED Wall Builder Task");
                    break;  // break out of the loop when done
                }
            }
        };
        wallBuilderThread = new Thread(wallBuilderTask, "wall-builder");
        wallBuilderThread.start();
    }

    /** Called when the user closes the app window */
    public void stopWallBuilder()
    {
        if(wallBuilderThread == null)
        {
            return;
        }
        wallBuilderThread.interrupt();
        wallBuilderThread = null;
    }
    
    /** Checks the map if the map already contains a wall in the coordinates */
    private boolean wallIsOccupied(Wall newWall)
    {
        if(wallMap.containsKey(newWall.getCoords()))
        {
            System.out.println(">>>INVALID: A wall already exist in "
                + "("+newWall.getCoords()+")");
            return true;
        }
        return false;
    }


    // ** DRAW METHODS **


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
        
        // ** Uses two executors to draw the Robots and Walls all at once **
        
        // Draw the robots all at once
        ExecutorService drawRobotsExecutor = Executors.newFixedThreadPool(robotLimit);
        robotMap.forEach((robotCoords, robot) ->
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

        // Draw the walls all at once
        ExecutorService drawWallsExecutor = Executors.newFixedThreadPool(wallLimit*2);
        wallMap.forEach((wallCoords, wall) ->
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
        tpManager.shutdownExecutor(drawRobotsExecutor);
        tpManager.shutdownExecutor(drawWallsExecutor);
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
     * You shouldn't need to modify this method.
     */
    // private void drawLine(GraphicsContext gfx, double gridX1, double gridY1, 
    //                                            double gridX2, double gridY2)
    // {
    //     gfx.setStroke(Color.RED);
        
    //     // Recalculate the starting coordinate to be one unit closer to the destination, so that it
    //     // doesn't overlap with any image appearing in the starting grid cell.
    //     final double radius = 0.5;
    //     double angle = Math.atan2(gridY2 - gridY1, gridX2 - gridX1);
    //     double clippedGridX1 = gridX1 + Math.cos(angle) * radius;
    //     double clippedGridY1 = gridY1 + Math.sin(angle) * radius;
        
    //     gfx.strokeLine((clippedGridX1 + 0.5) * gridSquareSize, 
    //                    (clippedGridY1 + 0.5) * gridSquareSize, 
    //                    (gridX2 + 0.5) * gridSquareSize, 
    //                    (gridY2 + 0.5) * gridSquareSize);
    // }
    
    /** Calls GUI Thread to request for layout */
    public void drawArena()
    {
        Platform.runLater(() ->
        {
            requestLayout();
        });
    }
    
    public void gameOver()
    {
        System.out.println("Game over!");
        ui.setLog("Game over!");
        stopAllRobots();
        stopWallBuilder();
        ui.stopAll();
    }
}
