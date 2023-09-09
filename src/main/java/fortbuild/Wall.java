package fortbuild;

public class Wall
{
    private int x;  // Where to place the wall at
    private int y;
    
    public Wall(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public String getCoords()
    {
        return (x+","+y);
    }
}
