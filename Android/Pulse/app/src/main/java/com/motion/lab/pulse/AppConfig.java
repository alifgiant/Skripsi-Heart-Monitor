package com.motion.lab.pulse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by maaakbar on 4/28/16.
 */
public class AppConfig {

    // region PAGE NAVIGATION
    public static void movePage(Context context, Class<?> cls, Bundle bundle) {
        Intent move = new Intent(context, cls);
        move.putExtra(context.getString(R.string.extra), bundle);
        context.startActivity(move);
    }
    public static void movePage(Context context, Class<?> cls) {
        Intent move = new Intent(context, cls);
        context.startActivity(move);
    }
    public static void movePageAndFinish(Context context, Class<?> cls, Bundle bundle) {
        movePage(context, cls, bundle);
        ((Activity)context).finish();
    }
    public static void movePageAndFinish(Context context, Class<?> cls) {
        movePage(context, cls);
        ((Activity)context).finish();
    }
    // endregion
}
