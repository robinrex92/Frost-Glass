package in.robinrex.frostglass;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author ContusTeam <developers@contus.in>
 * @version 1.0
 */

public class FrostableActivity extends AppCompatActivity {

    private FrostGlass mFrostGlass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFrostGlass = new FrostGlass(this);
    }

    public FrostGlass getFrostGlass() {
        return mFrostGlass;
    }

    public boolean staticFrost(int blurRadius) {
        return mFrostGlass != null && mFrostGlass.staticFrost(blurRadius);

    }

    public boolean liveFrost(int blurRadius) {
        return mFrostGlass != null && mFrostGlass.liveFrost(blurRadius);

    }

    public boolean defrost() {
        return mFrostGlass != null && mFrostGlass.defrostScreen();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mFrostGlass.isLive())
            mFrostGlass.resumeFrosting();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mFrostGlass.isLive())
            mFrostGlass.pauseFrosting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFrostGlass != null)
            mFrostGlass.destroy();
    }
}
