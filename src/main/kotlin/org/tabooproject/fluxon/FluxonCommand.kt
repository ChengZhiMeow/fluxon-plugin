package org.tabooproject.fluxon

import org.tabooproject.fluxon.runtime.FluxonRuntime
import org.tabooproject.fluxon.tool.FunctionDumper
import org.tabooproject.fluxon.util.exceptFluxonCompletableFutureError
import org.tabooproject.fluxon.util.tell
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.util.execution
import taboolib.common5.Demand
import taboolib.expansion.createHelper
import kotlin.time.measureTime

@CommandHeader("fluxon", aliases = ["fn"])
object FluxonCommand {

    @CommandBody
    private val main = mainCommand {
        createHelper()
    }

    @Suppress("DuplicatedCode")
    @CommandBody
    private val run = subCommand {
        dynamic("id") {
            suggest { FluxonLibrary.scripts.keys().toList() }
            dynamic("params") {
                exec<ProxyCommandSender> {
                    val params = Demand("0 ${ctx["params"]}".trim())
                    val vars = hashMapOf<String, Any?>()
                    params.tags.forEach { vars[it] = true }
                    params.dataMap.forEach { vars[it.key] = it.value }
                    val sender = sender
                    vars["sender"] = sender
                    val result: Any?
                    val time = measureTime {
                        result = FluxonLibrary.invoke(ctx["id"], vars)
                    }
                    sender.tell("执行结果: $result")
                    sender.tell("耗时: $time")
                }
            }
            exec<ProxyCommandSender> {
                val vars = hashMapOf<String, Any?>()
                val sender = sender
                vars["sender"] = sender
                val result: Any?
                val time = measureTime {
                    result = FluxonLibrary.invoke(ctx["id"], vars)
                }
                sender.tell("执行结果: $result")
                sender.tell("耗时: $time")
            }
        }
    }

    @CommandBody
    private val eval = subCommand {
        dynamic("script") {
            exec<ProxyCommandSender> {
                try {
                    val (result, time) = execution {
                        FluxonShell.invoke(ctx["script"], useCache = false) {
                            defineRootVariable("sender", sender)
                        }
                    }
                    result?.exceptFluxonCompletableFutureError()
                    sender.tell("Result: $result")
                    sender.tell("$time ms")
                } catch (ex: Throwable) {
                    sender.tell("Error: ${ex.message}")
                    ex.printStackTrace()
                }
            }
        }
    }

    @CommandBody
    private val list = subCommandExec<ProxyCommandSender> {
        sender.tell("已加载的 Fluxon 库文件:")
        FluxonLibrary.libraryLoader.managedResults.forEach { result ->
            sender.tell("- ${result.sourcePath}")
        }
        sender.tell("已加载的 Fluxon 脚本文件 (${FluxonLibrary.scripts.size}):")
        FluxonLibrary.scripts.forEach { (id, script) ->
            val timeDiff = System.currentTimeMillis() - script.timestamp
            val timeStr = when {
                timeDiff < 60000 -> "${timeDiff / 1000}秒前"
                timeDiff < 3600000 -> "${timeDiff / 60000}分钟前"
                else -> "${timeDiff / 3600000}小时前"
            }
            val statusInfo = buildList {
                add("加载: $timeStr")
                if (script.resources.isNotEmpty()) add("资源: ${script.resources.size}")
                if (script.initialized != null) add("已初始化")
            }.joinToString(", ")
            sender.tell("- $id")
            sender.tell("  类型: ${script.instance.javaClass.simpleName}")
            sender.tell("  文件: ${script.scriptFile.path}")
            sender.tell("  状态: $statusInfo")
        }
    }

    @CommandBody
    private val extensions = subCommandExec<ProxyCommandSender> {
        val entries = FluxonRuntime.getInstance().extensionFunctions.entries.sortedByDescending { it.value.size }
        sender.tell("当前扩展函数: (根据重复的函数名排序)")
        if (entries.size < 30) {
            entries.forEach {
                sender.tell("- ${it.key} ${it.value.keys.map { c -> c.simpleName }} (${it.value.size})")
            }
        } else {
            entries.take(30).forEach {
                sender.tell("- ${it.key} ${it.value.keys.map { c -> c.simpleName }} (${it.value.size})")
            }
            sender.tell("... 省略 ${entries.size - 20} 个 ...")
        }
        // 统计多候选类型的扩展函数总数
        val multiTypeCount = entries.count { it.value.size > 1 }
        sender.tell("多候选类型的扩展函数 $multiTypeCount 个")
        // 统计单个候选类型的扩展函数总数
        val singleTypeCount = entries.count { it.value.size == 1 }
        sender.tell("单候选类型的扩展函数: $singleTypeCount 个")
    }

    @CommandBody
    val dump = subCommandExec<ProxyCommandSender> {
        FunctionDumper().dumpToFile("fluxon-functions.json")
        sender.tell("已将 Fluxon 函数列表导出到 fluxon-functions.json 文件。")
    }

    @CommandBody
    private val reload = subCommand {
        dynamic("id", optional = true) {
            suggest { FluxonLibrary.scripts.keys().toList() }
            exec<ProxyCommandSender> {
                val id = ctx["id"]
                val script = FluxonLibrary.scripts[id]
                if (script == null) {
                    sender.tell("脚本 $id 不存在。")
                } else {
                    script.reload()
                    sender.tell("脚本 $id 重载完成。")
                }
            }
        }
        exec<ProxyCommandSender> {
            FluxonLibrary.reload()
            sender.tell("重载完成。")
        }
    }
}