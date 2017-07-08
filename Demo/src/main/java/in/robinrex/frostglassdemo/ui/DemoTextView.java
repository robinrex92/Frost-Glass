package in.robinrex.frostglassdemo.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import in.robinrex.frostglassdemo.DemoApp;

/**
 * Created by robin on 8/7/17.
 */

public class DemoTextView extends AppCompatTextView {

    public DemoTextView(Context context) {
        super(context);

        init();
    }

    public DemoTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public DemoTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setTypeface(DemoApp.getTypeface());
    }


}
