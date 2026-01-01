package org.tabooproject.fluxon.function

import org.tabooproject.fluxon.runtime.FluxonRuntime
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.adaptLocation
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.allWorlds
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers

object FunctionAdapter {

    @Awake(LifeCycle.INIT)
    private fun init() {
        with(FluxonRuntime.getInstance()) {
            registerFunction("console", 0) { console() }
            registerFunction("onlinePlayers", 0) { onlinePlayers() }
            registerFunction("adaptPlayer", 1) { adaptPlayer(it.getArgument(0)!!) }
            registerFunction("adaptCommandSender", 1) { adaptCommandSender(it.getArgument(0)!!) }
            registerFunction("adaptLocation", 1) { adaptLocation(it.getArgument(0)!!) }
            registerFunction("allWorlds", 0) { allWorlds() }
        }
    }
}