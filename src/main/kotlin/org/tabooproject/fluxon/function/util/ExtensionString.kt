package org.tabooproject.fluxon.function.util

import org.tabooproject.fluxon.runtime.FluxonRuntime
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common5.util.parseMillis
import taboolib.common5.util.parseUUID
import taboolib.module.chat.colored
import taboolib.module.chat.parseToHexColor
import taboolib.module.chat.uncolored

object ExtensionString {

    @Awake(LifeCycle.INIT)
    private fun init() {
        with(FluxonRuntime.getInstance()) {
            registerExtension(String::class.java)
                .function("parseMillis", 0) { it.target!!.parseMillis() }
                .function("parseUUID", 0) { it.target!!.parseUUID() }
                .function("colored", 0) { it.target!!.colored() }
                .function("uncolored", 0) { it.target!!.uncolored() }
                .function("parseToHexColor", 0) { it.target!!.parseToHexColor() }
        }
    }
}