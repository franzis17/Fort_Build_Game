package fortbuild;

import javafx.scene.control.*;

public class Player
{
    private Thread scoreThread;
    
    private int score;  private Object scoreMutex = new Object();
    private App app;
    
    public Player(App app)
    {
        score = 0;
        this.app = app;
    }
    
    /**
     * Every second, the score increases by 10 points
     */
    public void startScoreCount() throws IllegalStateException
    {
        if(scoreThread != null)
        {
            throw new IllegalStateException(
                "Tried to create a thread for startScoreCount() but thread already exists");
        }
        
        Runnable incrScoreTask = () ->
        {
            while(true)
            {
                try
                {
                    synchronized(scoreMutex)  // lock because increaseScore() is modified by another thread
                    {
                        score += 10;
                    }
                    app.logScore(score);
                    Thread.sleep(1000);
                }
                catch (InterruptedException ie)
                {
                    System.out.println("Interrupted Score count!");
                }
            }
        };
        scoreThread = new Thread(incrScoreTask, "score-thread");
        scoreThread.start();
    }
    
    public void stopScoreCount() throws IllegalStateException
    {
        if(scoreThread == null)
        {
            throw new IllegalStateException();
        }
        
        scoreThread.interrupt();
        scoreThread = null;
    }
    
    public void increaseScore(int addedScore)
    {
        // Each time a robot gets destroyed, increase points by 100
        synchronized(scoreMutex)
        {
            score += addedScore;
            app.logScore(score);
        }
    }
}
