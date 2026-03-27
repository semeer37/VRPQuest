package com.lex.vrpquest.Utils

import android.content.Context
import android.util.Log
import com.lex.vrpquest.Pages.LaunchErrorPage
import java.lang.Thread.UncaughtExceptionHandler

class CustomExceptionHandler(
    private val context: Context,
    private val default:  UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Handle the exception here (e.g., log it, send a crash report, etc.)
        Log.e("CustomExceptionHandler", "Uncaught exception: ", throwable)
        LaunchErrorPage(context, throwable)

        // Optionally, you can call the default exception handler to let the system handle the crash
        default?.uncaughtException(thread, throwable)
    }
}