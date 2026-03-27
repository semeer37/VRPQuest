package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.LaunchMainActivity
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.gradientBackground
import com.lex.vrpquest.Utils.launchAfterClose
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay



@RequiresApi(Build.VERSION_CODES.R)
fun getPermissionList(context: Context): List<Boolean> {
    return listOf(
        context.packageManager.canRequestPackageInstalls(),
        Environment.isExternalStorageManager()
    )
}

@RequiresApi(Build.VERSION_CODES.R)
fun IsAllPermissionAllowed(context: Context): Boolean {
    return !getPermissionList(context).contains(false)
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PermissionPage() {
    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)
    var InstallApkPerm by remember { mutableStateOf(context.packageManager.canRequestPackageInstalls()) }
    var FileAccessPerm by remember { mutableStateOf(Environment.isExternalStorageManager()) }
    LaunchedEffect(true) {
        while(true) {
            delay(10)
            InstallApkPerm = context.packageManager.canRequestPackageInstalls()
            FileAccessPerm = Environment.isExternalStorageManager()
            if (InstallApkPerm && FileAccessPerm) {
                break
            }
        }
        cancel()
    }


    fun grant() {
        if (!InstallApkPerm) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", context.packageName)))
            context.startActivity(intent)
        }
        if(!FileAccessPerm) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
    if (!(InstallApkPerm && FileAccessPerm)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}) {
            Box(modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .width(280.dp)
                .align(Alignment.Center)
                .background(
                    CustomColorScheme.surface
                )) {
                Column(Modifier.padding(20.dp)) {
                    Text(text = "These permissions are required by this application:", color = CustomColorScheme.onSurface)
                    Spacer(modifier = Modifier.size(20.dp))
                    Text(modifier = Modifier.padding(20.dp,0.dp),
                        color = if (InstallApkPerm) CustomColorScheme.tertiary else CustomColorScheme.error,
                        text = "- INSTALL APK")
                    Text(modifier = Modifier.padding(20.dp,0.dp),
                        color = if (FileAccessPerm) CustomColorScheme.tertiary else CustomColorScheme.error,
                        text = "- FILE ACCESS")

                    Spacer(modifier = Modifier.size(20.dp))
                    Row() {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1F)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.tertiary,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { grant() },
                        ) { Text(text = "GRANT") }
                        Spacer(modifier = Modifier.size(10.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1F)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { launchAfterClose({LaunchMainPage(context) }, activity) },
                        ) { Text(text = "EXIT", color = CustomColorScheme.onSurface) }
                    }
                }
            }
        }
    } else { launchAfterClose({ LaunchMainActivity(context) }, activity) }
}

class PermissionActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = CustomColorScheme) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .gradientBackground(CustomColorScheme.background, CustomColorScheme.onBackground)) {
                    PermissionPage()
                }
            }
        }
    }
}

fun LaunchPermissionPage(context: Context) {
    val intent = Intent(context, PermissionActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK )
    context.startActivity(intent)
}