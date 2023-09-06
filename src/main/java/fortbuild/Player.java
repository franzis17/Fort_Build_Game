package fortbuild;

import javafx.scene.control.*;

public class Player
{
    private Thread scoreThread;
    
    private int score;  private Object scoreMutex = new Object();
    private UserInterface ui;
    
    public Player()
    {
        score = 0;
    }
    
    public void setUI(UserInterface ui)
    {
        this.ui = ui;
    }
    
    public void startScoreCount()
    {
        try
        {
            startScoreThread();
        }
        catch(IllegalStateException ise)
        {
            System.out.println("ERROR When trying to start scoreThread: " + ise.getMessage());
        }
    }
    
    /**
     * Every second, the score increases by 10 points
     */
    public void startScoreThread() throws IllegalStateException
    {
        if(scoreThread != null)
        {
            throw new IllegalStateException(
                "Tried to create a thread for startScoreCount() but thread already exists");
        }
        
        Runnable incrScoreTask = () ->
        {
            try
            {
                while(true)
                {
                    synchronized(scoreMutex)  // lock because increaseScore() is modified by another thread
                    {
                        score += 10;
                    }
                    ui.setScore(score);
                    Thread.sleep(1000);
                }
            }
            catch(InterruptedException ie)
            {
                System.out.println("Interrupted Score count!");
            }
        };
        scoreThread = new Thread(incrScoreTask, "score-thread");
        scoreThread.start();
    }
    
    public void stopScoreCount()
    {
        try
        {
            stopScoreThread();
        }
        catch(IllegalStateException ise)
        {
            System.out.println("ERROR: " + ise.getMessage());
        }
    }
    
    public void stopScoreThread() throws IllegalStateException
    {
        if(scoreThread == null)
        {
            throw new IllegalStateException(
                "ERROR: Tried to stop scoreThread but it is not created yet");
        }
        
        scoreThread.interrupt();
        scoreThread = null;
    }
    
    /** 
     * Called by another thread when a robot gets destroyed by a wall
     */
    public void increaseScore(int addedScore)
    {
        // Each time a robot gets destroyed, increase points by 100
        synchronized(scoreMutex)
        {
            score += addedScore;
            ui.setScore(score);
        }
    }
}
