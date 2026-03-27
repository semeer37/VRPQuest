package com.lex.vrpquest.Pages

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.getDonateGames
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.RoundByteValue
import com.lex.vrpquest.Utils.SetTheme
import com.lex.vrpquest.Utils.TopSearchBar
import com.lex.vrpquest.Utils.gradientBackground
import com.lex.vrpquest.Utils.gradientScroll
import java.io.File

class MainPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetTheme(applicationContext)

            //hide mainpage when theres another activity covering it, this is required for themes with transparent backgrounds
            var IsHidden = remember { mutableStateOf(false) }
            lifecycle.addObserver(LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> { IsHidden.value = true }
                    Lifecycle.Event.ON_RESUME -> { IsHidden.value = false }
                    else -> {}
                }}
            )

            MaterialTheme(colorScheme = CustomColorScheme) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .gradientBackground(CustomColorScheme.background, CustomColorScheme.onBackground)
                    .alpha(if(IsHidden.value) 0F else 1F)) {
                    MainPage()
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainPage() {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(0) }
    var sortReversed by remember { mutableStateOf(false) }
    var Gamelist = remember {mutableStateListOf<Game>()}

    //DEBUG OPTION RUN WHITOUT METADATA REINSTALL
    //if(Gamelist.isEmpty()) { Gamelist.addAll(SortGameList(context, {println(it)})) }

    Column() {
        TopSearchBar(searchText, {searchText = it}, "Search here", {sortType = it},  {sortReversed = it})
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp, 0.dp)
        ) {

            if(Gamelist.isEmpty()) {
                MetadataLoader({ Gamelist.addAll(it) })
                //FullText("Game List is currently empty, gamedata is either deleted or there was an issue connecting to the VRP server")
            } else {
                //Donation Check ONLY RUN ONCE
                LaunchedEffect(Unit) { LaunchDonatePage(context, getDonateGames(context, Gamelist)) }

                var tempsortlist = Gamelist.filter { it.GameName.contains(searchText, ignoreCase = true) }
                when(sortType) {
                    1 -> { tempsortlist = tempsortlist.sortedBy { it.VersionCode } }
                    2 -> { tempsortlist = tempsortlist.sortedBy { it.Size } }
                    3 -> { tempsortlist = tempsortlist.sortedBy { it.LastUpdated } }
                }
                if (sortReversed) { tempsortlist = tempsortlist.reversed() }

                val lazygridstate = rememberLazyGridState()
                LazyVerticalGrid(
                    modifier = Modifier
                        .gradientScroll(lazygridstate),
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    state = lazygridstate
                ) {
                    items(tempsortlist) { game ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    CustomColorScheme.surface
                                )
                                .clickable { LaunchGameInfoPage(context, game) },
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                GlideImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.752336448598131F) //aspect ratio of thumbnail image in case its missing
                                        .clip(RoundedCornerShape(40.dp)),
                                    model = File(game.Thumbnail),
                                    contentDescription = ""
                                )
                                Spacer(modifier = Modifier.size(10.dp))
                                Text(
                                    text = game.GameName,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(20.dp, vertical = 0.dp),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = CustomColorScheme.onSurface
                                )
                                Text(
                                    text = RoundByteValue(game.Size) + " - v" + game.VersionCode,
                                    modifier = Modifier.padding(20.dp, vertical = 0.dp),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = CustomColorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun LaunchMainPage(context: Context) {
    val intent = Intent(context, MainPageActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK )
    context.startActivity(intent)
}