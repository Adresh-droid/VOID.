

package com.voidplayer.music.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconUtils {
    fun setIcon(context: Context, isDynamic: Boolean, isLegacy: Boolean) {
        val pm = context.packageManager
        val dynamic = ComponentName(context, "com.voidplayer.music.MainActivityAlias")
        val static = ComponentName(context, "com.voidplayer.music.MainActivityStatic")
        val legacy = ComponentName(context, "com.voidplayer.music.MainActivityLegacy")

        pm.setComponentEnabledSetting(
            dynamic,
            if (isDynamic && !isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            static,
            if (!isDynamic && !isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            legacy,
            if (isLegacy) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

