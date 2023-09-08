package fortbuild;

public class TimeSinceExecution {

    private long startTime;

    public TimeSinceExecution() {
        this.startTime = System.nanoTime();
    }

    /** Simply call this method to get the time since execution */
    public String getElapsedTime() {
        long elapsed = System.nanoTime() - startTime;

        // Convert nanoseconds to seconds
        double seconds = elapsed / 1_000_000_000.0;

        return String.format("%.2f seconds", seconds);
    }
    
}
