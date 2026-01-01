package org.tabooproject.fluxon

import org.tabooproject.fluxon.compiler.FluxonFeatures
import org.tabooproject.fluxon.runtime.FluxonRuntime
import taboolib.common.platform.Plugin
import taboolib.common.util.runSync

object FluxonPlugin : Plugin() {

    init {
        // 启用特性
        FluxonFeatures.DEFAULT_ALLOW_INVALID_REFERENCE = true
        FluxonFeatures.DEFAULT_ALLOW_REFLECTION_ACCESS = true
        FluxonFeatures.DEFAULT_ALLOW_JAVA_CONSTRUCTION = true
        FluxonFeatures.DEFAULT_PACKAGE_AUTO_IMPORT.addAll(listOf("fs:time", "fs:crypto", "fs:reflect", "fs:io"))
        // 注册主线程执行器
        FluxonRuntime.getInstance().setPrimaryThreadExecutor {
            runSync { it.run() }
        }
    }

    override fun onEnable() {
    }
}