package in.robinrex.frostglass;

/**
 * A simple benchmarking tool, to help optimize renderscript performance by tracking the time it takes to render each
 * frame when frosting is in progress.
 *
 * @author Robin Rex G.
 * @version 1.0
 */

public class FrostTimeTracker {

    private static long mStartTime = -1;

    private static int mProcessedFrames = 0;

    private static int mTotalTime = 0;

    private static long mFrameTime = -1;

    /**
     * Resets the time tracker.
     * */
    public static void reset() {
        mStartTime = -1;
        mProcessedFrames = 0;
        mTotalTime = 0;
        mFrameTime = -1;
    }

    /**
     * Method that has to be called before calling invalidate on the view that is being frosted, or to be called
     * before frosting any view or bitmap.
     * */
    public static void start() {
        mStartTime = System.currentTimeMillis();
    }

    /**
     * Method to be called after invalidate() call on the view that is being frosted.
     * */
    public static void frameComplete() {

        if (mStartTime == -1)
            throw new IllegalStateException("FrostTimeTracker not started.");

        long mStopTime = System.currentTimeMillis();
        mFrameTime = mStopTime - mStartTime;
        mTotalTime += mFrameTime;
        mProcessedFrames++;
    }

    /**
     * Method to get the average time it takes to frost a frame with any given settings to the renderscript library.
     * This method has to be invoked after a considerable number of frost calls (or invalidate calls), to get an
     * approximate aggregate of the frost times.
     *
     * @return Returns the average time it takes to frost a bitmap.
     * */
    public static int getAverageTime() {

        if (mFrameTime == -1)
            throw new IllegalStateException("FrostTimeTracker still running.");

        return (mTotalTime / mProcessedFrames);
    }

    /**
     * Method to print the average time it takes to frost a frame to be rendered. Same as {@link #getAverageTime()},
     * it has to be called after a considerable number of frost calls (or invalidate calls), to print an approximate
     * aggregate of the frost times.
     * */
    public static void printAverageTime() {
        if (mFrameTime == -1)
            throw new IllegalStateException("FrostTimeTracker still running.");

        Logger.info("======================================");
        Logger.info(" Average Frame draw time : " + getAverageTime());
        Logger.info("======================================");
    }
}
