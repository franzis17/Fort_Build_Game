package fortbuild;

public class Robot
{
    private int id;
    private int d;  // delay
    
    // Coordinates in the grid
    private int x;
    private int y;
    
    public Robot(int id, int d, int x, int y)
    {
        this.id = id;
        this.d = d;
        this.x = x;
        this.y = y;
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
        return ("Robot " + id);
    }
    
    public String getCoords()
    {
        return (x+","+y);
    }
    
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    // move randomly BUT towards the Citadel
    
}
