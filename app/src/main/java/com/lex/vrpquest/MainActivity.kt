package com.lex.vrpquest


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.lex.vrpquest.Managers.IfUpdateAvailable
import com.lex.vrpquest.Pages.IsAllPermissionAllowed
import com.lex.vrpquest.Pages.LaunchMainPage
import com.lex.vrpquest.Pages.LaunchPermissionPage
import com.lex.vrpquest.Pages.LaunchUpdatePage
import com.lex.vrpquest.Utils.CustomExceptionHandler
import com.lex.vrpquest.Utils.SetTheme
import com.lex.vrpquest.Utils.launchAfterClose

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")



class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //register error handler that launches ErrorPage on an exception
        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler(
            applicationContext,
            Thread.getDefaultUncaughtExceptionHandler())
        )

        setContent {
            val activity = (LocalContext.current as Activity)
            SetTheme(applicationContext)
            LaunchedEffect(Unit) {
            println("CHECKING VRAAH ${IfUpdateAvailable(applicationContext)}")}

            LaunchedEffect(Unit) {
                if (!IsAllPermissionAllowed(applicationContext)) {
                    launchAfterClose({LaunchPermissionPage(applicationContext) }, activity)
                } else if (IfUpdateAvailable(applicationContext)) {
                    launchAfterClose({LaunchUpdatePage(applicationContext) }, activity)
                } else {
                    launchAfterClose({LaunchMainPage(applicationContext) }, activity)
                }
            }
        }
    }
}

fun LaunchMainActivity(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK )
    context.startActivity(intent)
}
