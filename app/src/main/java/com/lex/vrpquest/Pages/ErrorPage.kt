package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.SetTheme
import com.lex.vrpquest.Utils.copyToClipboard
import com.lex.vrpquest.Utils.gradientBackground

@Composable
fun ErrorPage(message: String, cause: String) {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    val localClipboardManager = LocalClipboardManager.current
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {}
        .gradientBackground(CustomColorScheme.background, CustomColorScheme.onBackground)) {
        Box(modifier = Modifier
            .padding(10.dp)
            .width(600.dp)
            .clip(RoundedCornerShape(50.dp))
            .align(Alignment.Center)
            .background(CustomColorScheme.surface)) {
            Column(Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(20.dp)) {
                    Text("UH OH, an error occurred!", color = CustomColorScheme.onSurface)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(message, color = CustomColorScheme.onSurface)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(cause, color = CustomColorScheme.onSurface)
                    Spacer(modifier = Modifier.size(10.dp))
                }

                Row(Modifier.padding(10.dp, 0.dp)) {
                    Button(
                        modifier = Modifier
                            .weight(0.1F)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomColorScheme.tertiary,
                            contentColor = CustomColorScheme.onSurface
                        ),
                        onClick = {copyToClipboard(localClipboardManager, "$cause \n $message")},
                    ) { Text(text = "Copy to clipboard" ) }
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        modifier = Modifier
                            .weight(0.1F)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomColorScheme.error,
                            contentColor = CustomColorScheme.onSurface
                        ),
                        onClick = { activity?.finishAndRemoveTask()},
                    ) { Text(text = "CLOSE") }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetTheme(applicationContext)
            val cause = intent.getStringExtra("cause")
            val message = intent.getStringExtra("message")
            ErrorPage(message.toString(), cause.toString())
        }
    }
}

fun LaunchErrorPage(context:Context, throwable: Throwable) {
    val intent = Intent(context, ErrorActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK)
    intent.putExtra("message", throwable.message)
    intent.putExtra("cause", throwable.cause.toString())
    context.startActivity(intent)
}