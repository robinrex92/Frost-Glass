package in.robinrex.frostglass;

/**
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */

public class FrostTimeTracker {

    private static long mStartTime = -1;

    private static int mProcessedFrames = 0;

    private static int mTotalTime = 0;

    private static long mFrameTime = -1;

    public static void reset() {
        mStartTime = -1;
        mProcessedFrames = 0;
        mTotalTime = 0;
        mFrameTime = -1;
    }

    public static void start() {
        mStartTime = System.currentTimeMillis();
    }

    public static void frameComplete() {

        if (mStartTime == -1)
            throw new IllegalStateException("FrostTimeTracker not started.");

        long mStopTime = System.currentTimeMillis();
        mFrameTime = mStopTime - mStartTime;
        mTotalTime += mFrameTime;
        mProcessedFrames++;
    }

    public static int getAverageTime() {

        if (mFrameTime == -1)
            throw new IllegalStateException("FrostTimeTracker still running.");

        return (mTotalTime / mProcessedFrames);
    }

    public static void printAverageTime() {
        if (mFrameTime == -1)
            throw new IllegalStateException("FrostTimeTracker still running.");

        Logger.info("======================================");
        Logger.info(" Average Frame draw time : " + getAverageTime());
        Logger.info("======================================");
    }
}
