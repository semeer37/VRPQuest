package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.Managers.installUpdate
import com.lex.vrpquest.Managers.newestVersion
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.SetTheme
import com.lex.vrpquest.Utils.gradientBackground
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun UpdatePage() {
    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)
    var IsInstalling = remember { mutableStateOf(false) }
    var progress = remember { mutableStateOf(0.0F) }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {}
        .gradientBackground(CustomColorScheme.background, CustomColorScheme.onBackground)) {
        Box(modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .align(Alignment.Center)
            .background(CustomColorScheme.surface)) {
            Column(Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(20.dp)) {
                    Text("A new update is available! - $newestVersion", color = CustomColorScheme.onSurface)
                    if(IsInstalling.value) {
                        Row(Modifier.padding(10.dp)) {
                            CircularProgressIndicator(
                                progress = { progress.value },
                                modifier = Modifier.size(80.dp),
                                color = CustomColorScheme.tertiary,
                                strokeWidth = 10.dp,
                                strokeCap = StrokeCap.Round,
                                gapSize = 0.dp,
                                trackColor = CustomColorScheme.onSurface,
                            )
                            Text("Downloading latest release",
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(20.dp),
                                color = CustomColorScheme.onSurface
                            )
                        }
                    }
                }
                if(!IsInstalling.value) {
                      Row(Modifier.padding(10.dp, 0.dp)) {
                        Button(modifier = Modifier.weight(0.1F).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CustomColorScheme.tertiary,
                                contentColor = CustomColorScheme.onSurface),
                            onClick = {
                                IsInstalling.value = true
                                GlobalScope.launch {
                                    installUpdate(context, {progress.value = it}, {})
                                    activity.finish()
                                }
                            },
                        ) { Text(text = "Install" ) }
                        Spacer(modifier = Modifier.size(10.dp))
                        Button(modifier = Modifier.weight(0.1F).height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { activity.finishAndRemoveTask()},
                        ) { Text(text = "later") }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

class UpdateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetTheme(applicationContext)
            UpdatePage()
        }
    }
}

fun LaunchUpdatePage(context: Context) {
    val intent = Intent(context, UpdateActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK )
    context.startActivity(intent)
}