package in.robinrex.frostglassdemo;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

import in.robinrex.frostglass.FGLayout;
import in.robinrex.frostglass.FGView;
import in.robinrex.frostglass.FrostEngine;
import in.robinrex.frostglass.FrostGlass;
import in.robinrex.frostglass.FrostableActivity;
import in.robinrex.frostglass.Logger;

public class DemoActivity extends FrostableActivity {

    TextView demoView;

    FGView demoFrostView;

    FGLayout demoFrostLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        demoView = (TextView) findViewById(R.id.demoView);
        demoFrostView = (FGView) findViewById(R.id.demoFrostView);
        demoFrostLayout = (FGLayout) findViewById(R.id.demoFrostLayout);

        demoFrostView.frostWith(demoView);
        demoFrostLayout.frostWith(demoView);

//        ViewDragger.drag(demoView).setInavalidateOnDrag(false);

        ViewDragger.drag(demoFrostView);
        ViewDragger.drag(demoFrostLayout);

        //Animates a text in the screen.
        startTextChanger();

        autoToggleLiveMode();

        //Set the frost duration.
        getFrostGlass().setFrostingDuration(100);

        //Set the frosting amount. Higher number means more blurring, and faster.
        getFrostGlass().setFrostQuality(12);

        //set the radius of the blur.
//        staticFrost(12);


    }

    private void autoToggleLiveMode() {
        final Handler liveModeToggler = new Handler();

        Runnable liveModeRunnable = new Runnable() {

            @Override
            public void run() {

                Logger.debug("Toggling live mode");
                if(!demoFrostView.isLive()) {
                    demoFrostLayout.enableLiveMode();
                    demoFrostView.enableLiveMode();
                } else {
                    demoFrostLayout.disableLiveMode();
                    demoFrostView.disableLiveMode();
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

                if(charCount == helloWorld.length())
                    charCount = 0;

                demoView.setText(helloWorld.substring(0, charCount));
                charCount++;

                textChangeHandler.postDelayed(this, 500);
            }
        };

        textChangeHandler.postDelayed(textChangeRunnable, 500);
    }

    public void defrost(View view) {
        if(getFrostGlass().isFrosted())
            defrost();
        else
            liveFrost(12);
    }
}
