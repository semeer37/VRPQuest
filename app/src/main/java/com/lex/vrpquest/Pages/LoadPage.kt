package com.lex.vrpquest.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.MetadataInitialize
import com.lex.vrpquest.Managers.MetadataInitializeFTP
import com.lex.vrpquest.Managers.testfpt
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SettingStoreBoolean
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun MetadataLoader(metadata: (ArrayList<Game>) -> Unit) {
    val context = LocalContext.current

    var username by remember { mutableStateOf(SettingGetSting(context, "username") ?: "") }
    var password by remember { mutableStateOf(SettingGetSting(context, "pass") ?: "") }
    var host by remember { mutableStateOf(SettingGetSting(context, "host") ?: "") }

    var IsFTP by remember { mutableStateOf(SettingGetBoolean(context, "isPrivateFtp") ?: false) }

    var progress by remember { mutableStateOf(0f) }
    var isRun by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }

    var finished by remember { mutableStateOf(false) }

        LaunchedEffect(true) {
        GlobalScope.launch {
            val testftp = testfpt(username, password, host)
            if (!isRun) {
                if (IsFTP && testftp) {
                    MetadataInitializeFTP(context, {state = it }, {progress = it}, {metadata(it)})
                } else {
                    SettingStoreBoolean(context, "isPrivateFtp", false)
                    MetadataInitialize(context, {state = it }, {progress = it}, {metadata(it)})
                }
            }
            isRun = true
        }
    }

    when(state) {
        0 -> { text = "Downloading Metadata"}
        1 -> { text = "Extracting Metadata"}
        2 -> { text = "Parsing Metadata"}
        3 -> { finished = true
        }
    }
    if(!finished) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(100.dp))
                .align(Alignment.Center)
                .background(CustomColorScheme.background)
            ) {
                Row(Modifier.padding(10.dp)) {
                    Text(text, Modifier.align(Alignment.CenterVertically).padding(20.dp))
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        color = CustomColorScheme.tertiary,
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        trackColor = CustomColorScheme.onSurface,)
                }

            }
        }
    }
}
