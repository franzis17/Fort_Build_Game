package fortbuild;

public class Player
{
    private int score;
    private Object scoreMutex = new Object();
    
    public Player()
    {
        score = 0;
    }
    
    public void startScoreCount()
    {
        // Every second, the score increases by 10 points
        while(true)
        {
            try
            {
                synchronized(scoreMutex)
                {
                    score += 10;
                }
                Thread.sleep(1000);
            }
            catch (InterruptedException ie) { }
        }
    }
    
    public void increaseScore(int score)
    {
        // Each time a robot gets destroyed, increase points by 100
        synchronized(scoreMutex)
        {
            this.score += score;
        }
    }
}
