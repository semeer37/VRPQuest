package com.lex.vrpquest.Managers

import android.content.Context
import com.lex.vrpquest.Utils.downloadFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

val RawGitHubUrl = "https://raw.githubusercontent.com/metalex201/VRPquestRelease"
val GitHubUrl = "https://github.com/metalex201/VRPquestRelease"
var newestVersion = ""

suspend fun IfUpdateAvailable(context : Context) : Boolean {
    var isUpdateAvailable = false
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val checker = GlobalScope.launch {
        val localVersion = packageInfo.versionName
        newestVersion = URL(RawGitHubUrl + "/refs/heads/main/version").readText()
        println(localVersion.toString())
        println(newestVersion.toString())
        if(localVersion.toString().trim() != newestVersion.toString().trim()) {
            println("is not equal" + true)
            isUpdateAvailable = true
        }
    }
    checker.join()
    return isUpdateAvailable
}

fun installUpdate(context: Context, progress:(Float) -> Unit, close:() -> Unit) {
    val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
    downloadFile(GitHubUrl + "/releases/latest/download/VRPquest.apk",
        "$externalFilesDir/update.apk",
        "rclone/v69",
        progress
    )
    close()
    installApk(context, File("$externalFilesDir/update.apk"))
}