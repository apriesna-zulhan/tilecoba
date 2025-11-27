import javax.swing.*;

public class StopwatchTimer {
    private long startMillis;
    private long accumulatedMillis;
    private Timer swingTimer;

    public void start() {
        if (swingTimer != null && swingTimer.isRunning()) {
            return;
        }
        startMillis = System.currentTimeMillis();
        swingTimer = new Timer(1000, e -> {
            // Tick handler reserved for future UI updates
        });
        swingTimer.start();
    }

    public void stop() {
        if (swingTimer != null) {
            swingTimer.stop();
            accumulatedMillis += System.currentTimeMillis() - startMillis;
        }
    }

    public void reset() {
        accumulatedMillis = 0;
        startMillis = System.currentTimeMillis();
    }

    public int getElapsedSeconds() {
        long current = accumulatedMillis;
        if (swingTimer != null && swingTimer.isRunning()) {
            current += System.currentTimeMillis() - startMillis;
        }
        return (int) (current / 1000);
    }
}
