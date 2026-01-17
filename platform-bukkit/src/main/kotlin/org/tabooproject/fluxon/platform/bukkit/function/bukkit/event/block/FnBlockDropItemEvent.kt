package org.tabooproject.fluxon.platform.bukkit.function.bukkit.event.block

import org.bukkit.event.block.BlockDropItemEvent
import org.tabooproject.fluxon.runtime.FluxonRuntime
import taboolib.common.LifeCycle
import taboolib.common.Requires
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

@Requires(classes = ["org.bukkit.event.block.BlockDropItemEvent"])
@PlatformSide(Platform.BUKKIT)
object FnBlockDropItemEvent {

    @Awake(LifeCycle.INIT)
    fun init() {
        with(FluxonRuntime.getInstance()) {
            registerExtension(BlockDropItemEvent::class.java)
                .function("player", 0) { it.target?.player }
                .function("blockState", 0) { it.target?.blockState }
                .function("items", 0) { it.target?.items }
        }
    }
}
