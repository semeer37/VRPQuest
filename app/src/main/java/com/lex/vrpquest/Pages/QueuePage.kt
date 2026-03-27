package com.lex.vrpquest.Pages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Managers.DonateService
import com.lex.vrpquest.Managers.DownloadService
import com.lex.vrpquest.Managers.QueueGame
import com.lex.vrpquest.Managers.RemoveDonatedGame
import com.lex.vrpquest.Managers.RemoveQueueGame
import com.lex.vrpquest.Utils.BaseLayout
import com.lex.vrpquest.Utils.CircleButton
import com.lex.vrpquest.Utils.ColumnDropDown
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.FullText
import com.lex.vrpquest.Utils.GetGameBitmap
import com.lex.vrpquest.Utils.RoundDivider
import com.lex.vrpquest.Utils.gradientScroll
import java.util.Locale

@Composable
fun QueuePage(Queuelist: MutableList<QueueGame>, Donatelist: MutableList<DonateQueue>) {

    BaseLayout("Queue list") {
        if(Queuelist.isEmpty() && Donatelist.isEmpty()) {
            FullText("Queue List is currently empty, you can start an install through tapping on an app and tapping on install")
        } else {
            val context = LocalContext.current

            var gamelistDropDown by remember { mutableStateOf(true) }
            var donatelistDropDown by remember { mutableStateOf(true) }

            val scrollState = rememberLazyListState()
            LazyColumn(
                state = scrollState,
                modifier = Modifier.gradientScroll(scrollState),
            ) {
                ColumnDropDown(
                    "Donations",
                    donatelistDropDown,
                    {donatelistDropDown  = it},
                    Donatelist.isEmpty()) {
                    itemsIndexed(Donatelist) {index, it ->
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(CustomColorScheme.surface))
                        {
                            val appInfo = context.packageManager.getApplicationInfo(it.packageName.value, 0)
                            val appname = appInfo.loadLabel(context.packageManager).toString()

                            val icon = appInfo.loadIcon(context.packageManager)
                            val bitmap = remember(icon) { // Include icon in remember to reset when icon changes
                                if (icon.intrinsicWidth == 0 || icon.intrinsicHeight == 0) {
                                    Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                                } else {
                                    icon.toBitmap().run {
                                        if (icon.intrinsicWidth != 64) {
                                            Bitmap.createScaledBitmap(this, 64, 64, false)
                                        } else this
                                    }
                                }
                            }

                            val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

                            Row(Modifier.padding(15.dp)) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "Package Icon",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.size(20.dp))
                                Column(
                                    Modifier
                                        .weight(0.1f)
                                        .padding(10.dp)) {
                                   if(it.IsActive.value) {
                                       Box(modifier = Modifier
                                           .fillMaxSize()
                                           .weight(0.1f)) {
                                           when(it.state.value) {
                                               0 -> { Text(text = "Zipping: $appname", color = CustomColorScheme.onSurface) }
                                               1 -> { Text(text = "Uploading: $appname", color = CustomColorScheme.onSurface) }
                                           }
                                       }

                                       Box(modifier = Modifier
                                           .fillMaxSize()
                                           .weight(0.1f)) {
                                            LinearProgressIndicator(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .fillMaxWidth(),
                                                color = CustomColorScheme.tertiary,
                                                trackColor = CustomColorScheme.onSurface,
                                                progress = { it.MainProgress.value })
                                        }
                                    }
                                    else {
                                       Box(modifier = Modifier
                                           .fillMaxSize()
                                           .weight(0.1f)) {
                                           Text(modifier = Modifier.align(Alignment.CenterStart),
                                               text = appname,
                                               color = CustomColorScheme.onSurface)
                                       }
                                    }
                                }
                                Spacer(modifier = Modifier.size(20.dp))
                                CircleButton(Icon = Icons.Default.Close, onClick = {
                                    RemoveDonatedGame(index)
                                }, color = CustomColorScheme.error)

                            }
                        }
                    }
                }

                ColumnDropDown(
                    "Downloads",
                    gamelistDropDown,
                    {gamelistDropDown = it},
                    Queuelist.isEmpty()) {
                    itemsIndexed(Queuelist) { index, it ->
                        var IsDropDown by remember { mutableStateOf(false) }
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(CustomColorScheme.surface))
                        {
                            Box(modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))) {
                                Row(Modifier.padding(10.dp)) {
                                    Image(modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(40.dp))
                                        .align(Alignment.CenterVertically),
                                        bitmap = GetGameBitmap(it.game.Thumbnail),
                                        contentDescription = "")
                                    Spacer(modifier = Modifier.size(20.dp))
                                    Column(
                                        Modifier
                                            .weight(0.1f)
                                            .padding(20.dp)) {
                                        Box(modifier = Modifier
                                            .fillMaxSize()
                                            .weight(0.1f)) {
                                            Text(modifier = Modifier.align(Alignment.CenterStart),
                                                text = it.game.GameName,
                                                color = CustomColorScheme.onSurface)
                                        }
                                        if(it.IsActive.value) {
                                            Box(modifier = Modifier
                                                .fillMaxSize()
                                                .weight(0.1f)) {
                                                LinearProgressIndicator(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .fillMaxWidth(),
                                                    color = CustomColorScheme.tertiary,
                                                    trackColor = CustomColorScheme.onSurface,
                                                    progress = { it.MainProgress.value })
                                            }
                                        }
                                    }
                                    if(it.IsActive.value) {
                                        Spacer(modifier = Modifier.size(20.dp))
                                        CircleButton(modifier = Modifier.padding(0.dp, 30.dp),
                                            Icon = if (IsDropDown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            onClick = { IsDropDown = !IsDropDown }, color = CustomColorScheme.tertiary)
                                    }
                                    CircleButton(modifier = Modifier.padding(30.dp), Icon = Icons.Default.Close, onClick = {
                                        RemoveQueueGame(context, index)
                                    }, color = CustomColorScheme.error)
                                }
                            }
                            if(IsDropDown) {
                                Spacer(modifier = Modifier.size(10.dp))
                                RoundDivider()
                                Spacer(modifier = Modifier.size(10.dp))
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(50.dp, 0.dp)) {
                                    when(it.state.value) {
                                        0 -> { Text(text = "Downloading.", color = CustomColorScheme.onSurface) }
                                        1 -> { Text(text = "Extracting..", color = CustomColorScheme.onSurface) }
                                        2 -> { Text(text = "Moving obb.", color = CustomColorScheme.onSurface) }
                                        3 -> { Text(text = "Downloading obb...", color = CustomColorScheme.onSurface) }
                                        4 -> { Text(text = "Downloading apk", color = CustomColorScheme.onSurface) }
                                    }
                                }

                                for(i in it.progressList.indices) {
                                    Spacer(modifier = Modifier.size(20.dp))
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(50.dp, 0.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "7z." + String.format(Locale.getDefault(), "%03d", i + 1), color = CustomColorScheme.onSurface)
                                        Spacer(modifier = Modifier.size(10.dp))
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                                            color = CustomColorScheme.tertiary,
                                            trackColor = CustomColorScheme.onSurface,
                                            progress = { it.progressList[i] })
                                    }
                                }
                                Spacer(modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

class QueueActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var Queuelist:MutableList<QueueGame> = remember { DownloadService._queueList }
            var DonateQueueList:MutableList<DonateQueue> = remember { DonateService._DonateList }

            QueuePage(Queuelist, DonateQueueList)
        }
    }
}

fun LaunchQueuePage(context:Context) {
    val intent = Intent(context, QueueActivity::class.java)
    context.startActivity(intent)
}