package in.robinrex.frostglassdemo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import in.robinrex.frostglass.FGLayout;
import in.robinrex.frostglass.FGView;
import in.robinrex.frostglass.FrostableActivity;
import in.robinrex.frostglass.Logger;

// If you are just using a simple activity and want to blur that activity, you could just extend Frostable activity.
// It has helper methods built-in to that makes it easier to frost an activity. However, you could still frost any
// other activity, but you have to call the FrostGlass's lifecycle methods manually from onPause and onResume methods
// of the activity.
public class DemoActivity extends FrostableActivity {

    TextView demoView;

    TextView demoFrostLayoutTextView;

    FGView demoFrostView;

    FGLayout demoFrostLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        demoView = (TextView) findViewById(R.id.demoView);
//        demoFrostView = (FGView) findViewById(R.id.demoFrostView);
//        demoFrostLayout = (FGLayout) findViewById(R.id.demoFrostLayout);
//        demoFrostLayoutTextView = (TextView) findViewById(R.id.demoLayoutView);
//
//        // Set the background views that will be used for frosting. It has to be kept in mind that FGView and FGLayout
//        // should not be a child of the passed view. This will be fixed in the future versions.
//        demoFrostView.frostWith(demoView);
//        demoFrostLayout.frostWith(demoView);
//
//        ViewDragger.enableDragging(demoFrostView);
//        ViewDragger.enableDragging(demoFrostLayout);
//
//        // Animates a text in the screen to demonstrate live frosting.
        startTextChanger();
//
//        // Enables and disabled live frost mode on the demo view with a delay between every toggle.
////        autoToggleLiveMode();
//
//
//        //The following code is for activity blurring using FrostGlass.
//        //Set the frost duration.
        getFrostGlass().setFrostingDuration(100);
//
//        //Set the frosting amount. Higher number means more blurring, and faster.
        getFrostGlass().setDownsampleFactor(12);
//
//        getFrostGlass().setOverlayColor(Color.parseColor("#44000000"));

        //set the radius of the blur.
        staticFrost(12);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                liveFrost(12);
            }
        }, 5000);
    }

    private void autoToggleLiveMode() {
        final Handler liveModeToggler = new Handler();

        Runnable liveModeRunnable = new Runnable() {

            @Override
            public void run() {

                Logger.debug("Toggling live mode");
                if (!demoFrostLayout.isLive()) {
                    demoFrostLayout.enableLiveMode();
                    demoFrostLayoutTextView.setText(getString(R.string.message_live_mode));
                } else {
                    demoFrostLayout.disableLiveMode();
                    demoFrostLayoutTextView.setText(getString(R.string.message_static_mode));
                }

                liveModeToggler.postDelayed(this, 2000);
            }
        };

        liveModeToggler.postDelayed(liveModeRunnable, 2000);
    }

    private void startTextChanger() {

        final String helloWorld = "HelloWorld!";

        final Handler textChangeHandler = new Handler();

        Runnable textChangeRunnable = new Runnable() {
            int charCount = 0;

            @Override
            public void run() {

                if (charCount == helloWorld.length())
                    charCount = 0;

                demoView.setText(helloWorld.substring(0, charCount));
                charCount++;

                textChangeHandler.postDelayed(this, 500);
            }
        };

        textChangeHandler.postDelayed(textChangeRunnable, 500);
    }

    //Defrosts the frost glass and thus, making the activity clearly visible again.
    public void defrost(View view) {
        if (getFrostGlass().isFrosted()) {
            defrost();
        } else
            liveFrost(17);
    }
}
