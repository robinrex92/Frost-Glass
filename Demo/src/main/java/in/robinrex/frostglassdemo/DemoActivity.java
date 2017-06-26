package in.robinrex.frostglassdemo;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import in.robinrex.frostglass.FGLayout;
import in.robinrex.frostglass.FGView;
import in.robinrex.frostglass.FrostGlass;
import in.robinrex.frostglass.FrostableActivity;

public class DemoActivity extends FrostableActivity {

    TextView demoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        demoView = (TextView) findViewById(R.id.demoView);

        ViewDragger.drag(demoView).setInavalidateOnDrag(false);

        //Animates a text in the screen.
        startTextChanger();

        //Set the frost duration.
        getFrostGlass().setFrostingDuration(200);

        //Set the frosting amount. Higher number means more blurring, and faster.
        getFrostGlass().setFrostQuality(12);

        //set the radius of the blur.
        liveFrost(12);


    }

    private void startTextChanger() {

        final String helloWorld = "Hello World!";

        final Handler textChangeHandler = new Handler();

        Runnable textChangeRunnable = new Runnable() {
            int charCount = 0;
            @Override
            public void run() {

                if(charCount == helloWorld.length())
                    charCount = 0;

                demoView.setText(helloWorld.substring(0, charCount));
                charCount++;

                textChangeHandler.postDelayed(this, 1000);
            }
        };

        textChangeHandler.postDelayed(textChangeRunnable, 1000);
    }

    public void defrost(View view) {
        if(getFrostGlass().isLive())
            defrost();
        else
            liveFrost(12);
    }
}
