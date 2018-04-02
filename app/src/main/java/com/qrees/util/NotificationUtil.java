package com.qrees.util;

import android.app.NotificationManager;
import android.content.Context;

/**
 * Created by dharmraj on 31/7/17.
 */

public class NotificationUtil {
    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }
}
