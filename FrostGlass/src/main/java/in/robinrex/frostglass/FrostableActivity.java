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

    public void staticFrost(int blurRadius) {
        if(mFrostGlass != null)
            mFrostGlass.staticFrost(blurRadius);
    }

    public void liveFrost(int blurRadius) {
        if(mFrostGlass != null)
            mFrostGlass.liveFrost(blurRadius);
    }

    public void defrost() {
        if(mFrostGlass != null)
            mFrostGlass.defrost();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mFrostGlass.isLive())
            mFrostGlass.resumeFrost();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mFrostGlass.isLive())
            mFrostGlass.pauseFrost();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mFrostGlass != null)
            mFrostGlass.destroy();
    }
}
