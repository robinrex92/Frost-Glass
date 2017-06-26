# frost-glass
A Frost Glass effect library for android.

This library is a simple utilization of Android's Renderscript and ScriptIntrinsicBlur API.

There are three main components in this library.

1. FGView
2. FGLayout
3. FrostGlass

1. FGView is nothing but a view with frosted glass (Blurry View) effect. Simply add this view in your layout or through java.
And pass in the source view, which will be blurred.
    ```
    FGView fgview;
    fgview.frostWith(sourceView);
    ```
    
2. FGLayout is same as FGView, but with the obvious exception that it is a layout. It extends a frame layout. You can add children to this layout. The part of the layout which are not obscured by the children will be frosted.
    ```
    FGLayout fglayout;
    fglayout.frostWith(sourceView);
    ```
    
3. FrostGlass is a helper class, that is used to frost the entire content of the screen. Can be useful when you want to show a pop up or dialog, and get the focus of the user, by blurring the content behind them. However, the dialogs or popups, should have to be triggered/shown after initiating the frosting.
    ```
    FrostGlass fg = new FrostGlass(mActivity);
    fg.staticFrost(mFrostAnimationDuration);
    (or)
    fg.liveFrost(mFrostAnimationDuration);
    ```
    
**#staticFrost()** method will make the frost effect, with whatever content the screen was showing at that particular point of time the method was invoked. In case the content changes after the method has been called, it wont be visible on the frosted view.

**#liveFrost()** will frost the content continously, which means, even when the content of the screen changes, the frost effect will be shown on the live content. However, this uses more memory and processing than **#staticFrost()** and must be used sparsely.

The content can be defrosted by the following.
    ```
    fg.defrost();
    ```
        


