package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lex.vrpquest.Managers.DownloadService
import com.lex.vrpquest.Managers.FTPconnect
import com.lex.vrpquest.Managers.FTPfindApk
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.QueueGame
import com.lex.vrpquest.Managers.getFreeStorageSpace
import com.lex.vrpquest.Managers.md5Hash
import com.lex.vrpquest.Managers.startDownloadService
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.GetGameBitmap
import com.lex.vrpquest.Utils.LinkStringBuilder
import com.lex.vrpquest.Utils.RoundByteValue
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.getDirFullSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GameInfoPage(game: Game?, OnClose: () -> Unit) {
    if (game != null) {
        val freespace = getFreeStorageSpace()
        val context = LocalContext.current
        var IsEnoughSpace  by remember { mutableStateOf<Boolean?>(null) }
        var IsFTP = SettingGetBoolean(context, "isPrivateFtp") ?: false
        if (IsFTP) {
            LaunchedEffect(true) {
                CoroutineScope(Dispatchers.IO).launch {
                    //check If obb exists
                    val obbfolder = File("/storage/emulated/0/Android/obb/${game.PackageName}")
                    var obbsize = 0L
                    if (obbfolder.exists()) {
                        obbsize = obbfolder.length()
                    }

                    var username = SettingGetSting(context, "username") ?: ""
                    var password = SettingGetSting(context, "pass") ?: ""
                    var host = SettingGetSting(context, "host") ?: ""

                    val client = FTPconnect(username, password, host) ?: return@launch
                    val remoteApk = FTPfindApk(client, "/Quest Games/" + game.ReleaseName + "/")
                    val file = File(remoteApk).name
                    val apksize = client.getSize(file).toLong()
                    IsEnoughSpace = (apksize + (game.Size * 1000000L) - obbsize) < freespace
                    client.disconnect()
                }
            }
        } else {
            var tempsize = 0L

            val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
            val gamehash =  md5Hash(game.ReleaseName + "\n")

            val hashfolder = File("$externalFilesDir/$gamehash/")
            val extractFolder = File("$externalFilesDir/${game.ReleaseName}/")

            if (hashfolder.exists()) { tempsize += getDirFullSize(hashfolder) }
            if (extractFolder.exists()) { tempsize += getDirFullSize(extractFolder) }

            IsEnoughSpace = ((game.Size * 1000000L) - tempsize) < freespace
            println("tempsize : $tempsize")
            println("isenoughspace : $IsEnoughSpace")
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}) {
            Box(modifier = Modifier
                .padding(10.dp)
                .width(600.dp)
                .clip(RoundedCornerShape(50.dp))
                .align(Alignment.Center)
                .background(
                    CustomColorScheme.surface
                )) {
                Column(
                    Modifier
                        .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                    Row() {
                        Image(modifier = Modifier
                            .clip(RoundedCornerShape(40.dp))
                            .height(100.dp)
                            .clip(RoundedCornerShape(40.dp)),
                            bitmap = GetGameBitmap(game.Thumbnail),
                            //contentScale = ContentScale.Crop,
                            contentDescription = "")

                        Spacer(modifier = Modifier.size(10.dp))
                        Text(modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(0.8f),
                            text = game.GameName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            color = CustomColorScheme.onSurface)
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                    Spacer(modifier = Modifier.size(10.dp))

                    Row ( if (game.notes != "") Modifier.height(200.dp) else Modifier
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(0.5f),
                            lineHeight = 27.sp,
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Release Name: ")
                                }
                                append(game.ReleaseName)
    
                                append("\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Version Code: ")
                                }
                                append(game.VersionCode)
                                append("\n")
    
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Last Updated: ")
                                }
                                append(game.LastUpdated)
                                append("\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Size: ")
                                }
                                append(RoundByteValue(game.Size))
                            },
                            color = CustomColorScheme.onSurface
                        )
                        if(game.notes != "") {
                            Spacer(modifier = Modifier.size(10.dp))
                            Column(modifier = Modifier
                                .weight(0.5F).verticalScroll(rememberScrollState())
                            )  {
                                Text(
                                    text = LinkStringBuilder(game.notes),
                                    color = CustomColorScheme.onSurface)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(10.dp))
                    
                    Row(Modifier.padding(10.dp, 0.dp)) {
                        if (IsEnoughSpace == true || IsEnoughSpace == null) {
                            Button(
                                modifier = Modifier
                                    .weight(0.1F)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.tertiary,
                                    contentColor = CustomColorScheme.onSurface
                                ),
                                onClick = {
                                            if(!DownloadService._queueList.any { it.game.PackageName == game.PackageName }) {
                                                DownloadService._queueList.add(QueueGame(game))
                                            }

                                            DownloadService._queueList.forEach { println("QUEUELIST VRAAH:" + it) }

                                            startDownloadService(context)
                                            OnClose()
                                          },
                            ) { Text(text =  if (IsEnoughSpace == null) "Checking if theres enough space" else "INSTALL") }
                        } else {
                            Button(
                                modifier = Modifier
                                    .weight(0.1F)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.error,
                                    contentColor = CustomColorScheme.onSurface
                                ),
                                onClick = {},
                            ) { Text(text = "NO SPACE" ) }
                        }

                        Spacer(modifier = Modifier.size(10.dp))
                        Button(
                            modifier = Modifier
                                .weight(0.1F)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { OnClose() },
                        ) { Text(text = "CLOSE") }

                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}


class GameInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val activity = (LocalContext.current as? Activity)
            val game = intent.getParcelableExtra<Game>("GameInfo")
            GameInfoPage(game, {activity?.finish()})
        }
    }
}

fun LaunchGameInfoPage(context:Context, game: Game) {
    val intent = Intent(context, GameInfoActivity::class.java)
    intent.putExtra("GameInfo", game) // "GAME_KEY" is a key to retrieve the object
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    context.startActivity(intent)
}

