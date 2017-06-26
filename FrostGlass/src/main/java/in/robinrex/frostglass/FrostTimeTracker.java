package in.robinrex.frostglass;

/**
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */

public class FrostTimeTracker {

    private static long mStartTime = 0;

    private static int mProcessedFrames = 0;

    private static int mTotalTime = 0;

    private static long mFrameTime = 0;

    public static void reset() {

    }

    public static void start() {
        mStartTime = System.currentTimeMillis();
    }

    public static void frameComplete() {

        long mStopTime = System.currentTimeMillis();
        mFrameTime = mStopTime - mStartTime;
        mTotalTime+=mFrameTime;
        mProcessedFrames++;
    }

    public static int getAverageTime() {
        return (mTotalTime / mProcessedFrames);
    }

    public static void printAverageTime() {
        Logger.info("======================================");
        Logger.info("| Average Frame draw time : " + getAverageTime());
        Logger.info("======================================");
    }
}
