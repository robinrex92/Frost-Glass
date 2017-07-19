package in.robinrex.frostglassdemo;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import in.robinrex.frostglass.FGLayout;
import in.robinrex.frostglass.FrostableActivity;
import in.robinrex.frostglass.Logger;

// If you are just using a simple activity and want to blur that activity, you could just extend Frostable activity.
// It has helper methods built-in to that makes it easier to frost an activity. However, you could still frost any
// other activity, but you have to call the FrostGlass's lifecycle methods manually from onPause and onResume methods
// of the activity.
public class DemoActivity extends FrostableActivity {

    TextView demoView;

    TextView demoFrostLayoutTextView;

    FGLayout demoFrostView;

    FGLayout demoFrostLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        demoView = (TextView) findViewById(R.id.demoView);
        demoFrostLayout = (FGLayout) findViewById(R.id.demoFrostLayout);
        demoFrostLayoutTextView = (TextView) findViewById(R.id.demoLayoutView);

        // Set the background views that will be used for frosting. It has to be kept in mind that FGView and FGLayout
        // should not be a child of the passed view. This will be fixed in the future versions.
        demoFrostLayout.frostWith(demoView);

        demoFrostLayout.setLiveMode(true);

        ViewDragger.enableDragging(demoFrostLayout);
        // Animates a text in the screen to demonstrate live frosting.
        startTextChanger();

        //The following code is for activity blurring using FrostGlass.
        //Set the frost duration.
        getFrostGlass().setFrostingDuration(100);

        //Set the frosting amount. Higher number means more blurring, and faster.
        getFrostGlass().setDownsampleFactor(8);

        //Sets the overlay color of the frost view
        getFrostGlass().setOverlayColor(Color.parseColor("#55000000"));

    }

    private void autoToggleLiveMode() {
        final Handler liveModeToggler = new Handler();

        Runnable liveModeRunnable = new Runnable() {

            @Override
            public void run() {

                Logger.debug("Toggling live mode");
                if (!demoFrostLayout.isLive()) {
                    demoFrostLayout.setLiveMode(true);
                    demoFrostLayoutTextView.setText(getString(R.string.message_live_mode));
                } else {
                    demoFrostLayout.setLiveMode(false);
                    demoFrostLayoutTextView.setText(getString(R.string.message_static_mode));
                }

                liveModeToggler.postDelayed(this, 2000);
            }
        };

        liveModeToggler.postDelayed(liveModeRunnable, 2000);
    }

    private void startTextChanger() {

        final String helloWorld = "Click Here";

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
        final View test = findViewById(R.id.test);
        if (getFrostGlass().isFrosted()) {

            Logger.debug("Trying to defrost");
            ValueAnimator recessAnimator = ValueAnimator.ofFloat(0.9f, 1);
            recessAnimator.setDuration(100);
            recessAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
            recessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    test.setScaleX((Float) animation.getAnimatedValue());
                    test.setScaleY((Float) animation.getAnimatedValue());
                }
            });

            if(defrost())
                recessAnimator.start();
        } else {

            Logger.debug("Trying to frost");
            ValueAnimator recessAnimator = ValueAnimator.ofFloat(1, 0.9f);
            recessAnimator.setDuration(100);
            recessAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
            recessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    Logger.debug("Defrost : "+animation.getAnimatedValue());
                    test.setScaleX((Float) animation.getAnimatedValue());
                    test.setScaleY((Float) animation.getAnimatedValue());
                }
            });



            Dialog dialog = new Dialog(this);
            dialog.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
            dialog.getWindow().setDimAmount(0f);
            dialog.setContentView(R.layout.dialog);
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    Logger.debug("Trying to frost i");

                    ValueAnimator recessAnimator = ValueAnimator.ofFloat(0.9f, 1);
                    recessAnimator.setDuration(100);
                    recessAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
                    recessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            test.setScaleX((Float) animation.getAnimatedValue());
                            test.setScaleY((Float) animation.getAnimatedValue());
                        }
                    });

                    if(defrost())
                        recessAnimator.start();
                }
            });

            if(liveFrost(8))
                recessAnimator.start();
        }

    }
}
