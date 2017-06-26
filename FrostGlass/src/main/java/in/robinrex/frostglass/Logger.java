/*
 *  @category Daimler
 *  @copyright Copyright (C) 2017 Contus. All rights reserved.
 *  @license http://www.apache.org/licenses/LICENSE-2.0
 */

package in.robinrex.frostglass;

import android.util.Log;

/**
 * Logger class. Used to debug code.
 *
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */
public class Logger {

    private static final String TAG = "FrostGlass";

    private Logger() {
        throw new IllegalAccessError("Not allowed");
    }

    /**
     * Prints debug log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     */
    public static void debug(Object message) {
        Log.d(TAG, message == null ? "null" : message.toString());
    }

    /**
     * Prints debug log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     * @param tag     The object whose class name will be used for tag.
     */
    public static void debug(Object tag, Object message) {
        Log.d(tag.getClass().getSimpleName(), message == null ? "null" : message.toString());
    }

    /**
     * Prints error log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     */
    public static void error(Object message) {
        Log.e(TAG, message == null ? "null" : message.toString());
    }

    /**
     * Prints error log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     * @param tag     The object whose class name will be used for tag.
     */
    public static void error(Object tag, Object message) {
        Log.e(tag.getClass().getSimpleName(), message == null ? "null" : message.toString());
    }

    /**
     * Prints warning log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     */
    public static void warn(Object message) {
        Log.w(TAG, message == null ? "null" : message.toString());
    }

    /**
     * Prints warn log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     * @param tag     The object whose class name will be used for tag.
     */
    public static void warn(Object tag, Object message) {
        Log.e(tag.getClass().getSimpleName(), message == null ? "null" : message.toString());
    }

    /**
     * Prints info log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     */
    public static void info(Object message) {
        Log.i(TAG, message == null ? "null" : message.toString());
    }

    /**
     * Prints info log of the given message in console.
     *
     * @param message The message object. The function converts it to string before printing the log.
     * @param tag     The object whose class name will be used for tag.
     */
    public static void info(Object tag, Object message) {
        Log.i(tag.getClass().getSimpleName(), message == null ? "null" : message.toString());
    }
}