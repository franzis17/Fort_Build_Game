package fortbuild;

import javafx.scene.control.*;

public class Player
{
    private Thread scoreThread;
    
    private int score = 0;  private Object scoreMutex = new Object();
    private UserInterface ui;
    
    public Player(UserInterface ui)
    {
        this.ui = ui;
    }
    
    /**
     * Every second, the score increases by 10 points
     */
    public void startScoreCount() throws IllegalStateException
    {
        if(scoreThread != null)
        {
            throw new IllegalStateException("Tried to create a thread for "
                + "startScoreCount() but thread already exists");
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
    
    public void stopScoreCount() throws IllegalStateException
    {
        if(scoreThread == null)
        {
            throw new IllegalStateException("Tried to stop scoreThread "
                + "but it is not created yet");
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
