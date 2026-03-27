package com.lex.vrpquest.Managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.lex.vrpquest.Utils.CustomColorScheme
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SevenZipExtract
import com.lex.vrpquest.Utils.downloadFile
import com.lex.vrpquest.Utils.showNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Game(
    val GameName:String,
    val ReleaseName:String,
    val PackageName:String,
    val VersionCode:String,
    val LastUpdated:String,
    val Thumbnail: String,
    val Size:Int,
    var IsInstalled:Boolean,
    var notes: String
) : Parcelable { //required for passing class between intent/activity
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readBoolean(),
        parcel.readString() ?: ""
    )

    // Write object values to Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(GameName)
        parcel.writeString(ReleaseName)
        parcel.writeString(PackageName)
        parcel.writeString(VersionCode)
        parcel.writeString(LastUpdated)
        parcel.writeString(Thumbnail)
        parcel.writeInt(Size)
        parcel.writeBoolean(IsInstalled)
        parcel.writeString(notes)
    }

    // Describe contents (usually 0)
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to create instances of Parcelable class
    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game {
            return Game(parcel)
        }

        override fun newArray(size: Int): Array<Game?> {
            return arrayOfNulls(size)
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun MetadataInitialize(context:Context, state: (Int) -> Unit, progress: (Float) -> Unit, metadata: (ArrayList<Game>) -> Unit) {

        Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
        val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
        if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
        if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
        Log.i(context.applicationInfo.name, "Downloading meta.7z")

        var testjson = JSONObject()

       if(isLinkOnlineOkHttp("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json")) {
           testjson = JSONObject(URL("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json").readText())
       } else {
           disableSSLCertificateChecking()
           testjson = JSONObject(URL("https://vrpirates.wiki/downloads/vrp-public.json").readText())
           enableSSLCertificateChecking()
       }



        val baseUri = testjson.getString("baseUri")
        val password = String(Base64.decode(testjson.getString("password")))
        state(0)
        println("$baseUri" + "meta.7z")
        downloadFile("$baseUri" + "meta.7z", "$externalFilesDir/meta.7z","rclone/v69", { progress(it/3) }).toString()
        state(1)
        SevenZipExtract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", true, password, { progress(0.33F + it/3) });
        state(2)
        metadata(SortGameList(context, { progress(0.66F + it/3) }))
        progress(1F)
        state(3)
        //zip(File("$externalFilesDir/meta"), File("$externalFilesDir/meta.zip"))
}
@OptIn(ExperimentalEncodingApi::class)
fun MetadataInitializeFTP(context:Context, state: (Int) -> Unit, progress: (Float) -> Unit, metadata: (ArrayList<Game>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
        try {
            Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
            val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
            if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
            if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
            Log.i(context.applicationInfo.name, "Downloading meta")

            var username = SettingGetSting(context, "username") ?: ""
            var password = SettingGetSting(context, "pass") ?: ""
            var host = SettingGetSting(context, "host") ?: ""

            val client = FTPconnect(username, password, host) ?: return@launch

            state(0)

            println("FTP metadata download")
            FTPdownloadFile(client, "$externalFilesDir/meta.7z", "/Quest Games/meta.7z", { progress(it) })

            state(1)
            SevenZipExtract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", true, "", { progress(it) });

            delay(100)

            state(2)
            metadata(SortGameList(context, { progress(it) }))

            state(3)
        } catch (e: Exception) {
            showNotification(context, "METADAT INSTALL FAILED")
        }
    }
}


fun SortGameList(context: Context, progress: (Float) -> Unit):ArrayList<Game> {
    val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
    var gamelist:ArrayList<Game> =  ArrayList<Game>()

    val pfile = File("$externalFilesDir/meta/VRP-GameList.txt")
    if (pfile.exists()) {
        val gamedata = pfile.readText().drop(pfile.readText().indexOf("\n") + 1).split("\n")
        val gamesize = gamedata.count()
        val EmptyThumb:Bitmap = Bitmap.createBitmap(374, 214, Bitmap.Config.ARGB_8888)
        EmptyThumb.eraseColor(CustomColorScheme.background.toArgb())
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true


        gamedata.forEachIndexed  { index, value ->
            var tempvalue = value.split(";")
            if (tempvalue.size > 4) {
                val thumbnail = "${externalFilesDir}/meta/.meta/thumbnails/${tempvalue[2]}.jpg"
                val notesFile = File("${externalFilesDir}/meta/.meta/notes/${tempvalue[1]}.txt")

                gamelist.add(
                    Game(
                    GameName = tempvalue[0],
                    ReleaseName = tempvalue[1],
                    PackageName = tempvalue[2],
                    VersionCode = tempvalue[3],
                    LastUpdated = tempvalue[4],
                    Size = tempvalue[5].toInt(),
                    Thumbnail = if (File(thumbnail).exists()) thumbnail else "",
                    IsInstalled = false,
                    notes = if (notesFile.exists()) { notesFile.readText() } else ""
                )
                )
            }
            progress((index + 1F) / gamesize)
        }
        progress(1F)
        return gamelist
    }
    return gamelist
}

fun isLinkOnlineOkHttp(url: String): Boolean {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .head() // Use HEAD request
            .build()

        val response = client.newCall(request).execute()
        println("test response :" + response.isSuccessful)
        return response.isSuccessful
    } catch (E: Exception) {
        return false
    }
}