package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.Managers.DonateGame
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Managers.DonateService
import com.lex.vrpquest.Managers.startDonateService
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.SettingStoreStringSet

val checkboxcolor = CheckboxColors(
    checkedCheckmarkColor = CustomColorScheme.onTertiary,
    uncheckedCheckmarkColor = CustomColorScheme.error,
    checkedBoxColor = CustomColorScheme.tertiary,
    uncheckedBoxColor = CustomColorScheme.surface,
    disabledCheckedBoxColor = Color.Transparent,
    disabledUncheckedBoxColor = Color.Transparent,
    disabledIndeterminateBoxColor = Color.Transparent,
    checkedBorderColor = Color.Transparent,
    uncheckedBorderColor = CustomColorScheme.error,
    disabledBorderColor = Color.Transparent,
    disabledUncheckedBorderColor = Color.Transparent,
    disabledIndeterminateBorderColor = Color.Transparent,
)

@Composable
fun DonatePage(donatelist: MutableList<DonateGame>) {
    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)
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
            Column(modifier = Modifier.padding(10.dp)) {
                Text(modifier = Modifier.padding(15.dp), text = "Donatable Apps were found", color = CustomColorScheme.onSurface)
                Text(modifier = Modifier.padding(15.dp), text = "All Apps are donated by users! Without them none of this would be possible!", color = CustomColorScheme.onSurface)
                Column(modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()) ) {
                    donatelist.forEach() { donategame ->
                        val applicationInfo = context.packageManager.getApplicationInfo(donategame.packageinfo.packageName, 0)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Box(){
                                Checkbox(checked = donategame.checked.value,
                                    onCheckedChange = { donategame.checked.value = it },
                                    colors = checkboxcolor
                                )
                                if (!donategame.checked.value) {
                                    Icon(Icons.Rounded.Close, "", Modifier.align(Alignment.Center), CustomColorScheme.error)
                                }
                            }

                            Text(text = applicationInfo.loadLabel(context.getPackageManager()).toString(), color = CustomColorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 10.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColorScheme.tertiary,
                        contentColor = CustomColorScheme.onSurface
                    ),
                    onClick = {
                        ProcessCheckList(context, donatelist)
                        activity.finishAndRemoveTask()
                    },
                ) { Text(text = "CONTINUE", color = CustomColorScheme.onSurface) }
            }
        }
    }
}

fun ProcessCheckList(context: Context, donatelist: List<DonateGame>) {
    val getblacklist = SettingGetStringSet(context, "DonateBlacklist") ?: mutableSetOf()
    var blacklist = getblacklist.toMutableSet()

    var returned:ArrayList<DonateQueue> = ArrayList<DonateQueue>()

    donatelist.forEach() {
        if (it.checked.value == false) {
            blacklist.add(it.packageinfo.packageName)
        } else {
            returned.add(DonateQueue(mutableStateOf(it.packageinfo.packageName)))
        }
    }

    SettingStoreStringSet(context, "DonateBlacklist", blacklist)

    if(donatelist.size != 0) {
        returned.forEach() { DonateService._DonateList.add(DonateQueue(packageName = it.packageName, IsActive = mutableStateOf(false))) }
        startDonateService(context)
    }
}

fun RemoveFromCheckList(context: Context, PackageToRemove: String) {
    val getblacklist = SettingGetStringSet(context, "DonateBlacklist") ?: mutableSetOf()
    var blacklist = getblacklist.toMutableSet()
    blacklist.add(PackageToRemove)
    SettingStoreStringSet(context, "DonateBlacklist", blacklist)
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class DonateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val donatelist = intent.getParcelableArrayListExtra<DonateGame>("DonateGames")
            DonatePage(donatelist?.toMutableList() ?: mutableListOf())
        }
    }
}

fun LaunchDonatePage(context:Context, donatelist: MutableList<DonateGame>) {
    if(donatelist.isNotEmpty()) {
        val intent = Intent(context, DonateActivity::class.java)
        intent.putParcelableArrayListExtra("DonateGames", ArrayList(donatelist))
        context.startActivity(intent)
    }
}