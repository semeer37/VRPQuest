package com.lex.vrpquest.Utils

import android.app.Activity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun launchAfterClose(activityfun:() -> Unit, activity: Activity) {
    GlobalScope.launch {
        activityfun()
    }
    activity.finishAndRemoveTask()
}