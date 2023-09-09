package fortbuild;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtility
{
    /** Returns a random number that is inclusively between min and max */
    public static int getRandomNum(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max+1);
    }
}
