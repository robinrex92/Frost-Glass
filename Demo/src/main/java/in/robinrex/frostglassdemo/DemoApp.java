package in.robinrex.frostglassdemo;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by robin on 8/7/17.
 */

public class DemoApp extends Application {

    static Typeface mTypeface;

    private static DemoApp mApp;

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;

        loadFonts();
    }

    private static void loadFonts() {
        mTypeface = Typeface.createFromAsset(mApp.getAssets(), "fonts/SourceSansPro-Light.otf");
    }

    public static Typeface getTypeface() {
        return mTypeface;
    }
}
