package org.tabooproject.fluxon.function

import org.tabooproject.fluxon.FluxonScript
import org.tabooproject.fluxon.runtime.Environment
import org.tabooproject.fluxon.runtime.FluxonRuntime
import org.tabooproject.fluxon.runtime.Function
import org.tabooproject.fluxon.runtime.FunctionContextPool
import org.tabooproject.fluxon.runtime.java.Export
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.function.registerCommand
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.platform.function.warning

object FunctionCommand {

    @Awake(LifeCycle.INIT)
    private fun init() {
        with(FluxonRuntime.getInstance()) {
            exportRegistry.registerClass(CommandBuilder::class.java)
            registerFunction("command", 1) { CommandBuilder(it.getString(0)!!, it.environment) }
            registerFunction("unregisterCommand", 1) { unregisterCommand(it.getString(0)!!) }
        }
    }

    class CommandBuilder(val name: String, val env: Environment) {

        var aliases = emptyList<String>()
        var description = ""
        var usage = ""
        var permission = ""
        var permissionMessage = ""
        var permissionDefault = PermissionDefault.OP
        var executor: Function? = null
        var completer: Function? = null

        @Export
        fun aliases(aliases: List<String>): CommandBuilder {
            this.aliases = aliases
            return this
        }

        @Export
        fun description(description: String): CommandBuilder {
            this.description = description
            return this
        }

        @Export
        fun usage(usage: String): CommandBuilder {
            this.usage = usage
            return this
        }

        @Export
        fun permission(permission: String): CommandBuilder {
            this.permission = permission
            return this
        }

        @Export
        fun permissionMessage(permissionMessage: String): CommandBuilder {
            this.permissionMessage = permissionMessage
            return this
        }

        @Export
        fun permissionDefault(permissionDefault: String): CommandBuilder {
            this.permissionDefault = PermissionDefault.valueOf(permissionDefault.uppercase())
            return this
        }

        @Export
        fun executor(executor: Function): CommandBuilder {
            this.executor = executor
            return this
        }

        @Export
        fun completer(completer: Function): CommandBuilder {
            this.completer = completer
            return this
        }

        @Export
        fun register() {
            val script = env.rootVariables["__script__"] as? FluxonScript
            if (script == null) {
                warning("无法注册 $name 命令：没有找到脚本环境。")
                return
            }
            registerCommand(
                command = CommandStructure(
                    name,
                    aliases = aliases,
                    description = description,
                    usage = usage,
                    permission = permission,
                    permissionMessage = permissionMessage,
                    permissionDefault = permissionDefault,
                    permissionChildren = emptyMap(),
                    newParser = false,
                ),
                executor = object : CommandExecutor {
                    override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean {
                        if (executor != null) {
                            val pool = FunctionContextPool.local()
                            val borrowed = pool.borrow(executor!!, null, arrayOf(args), env)
                            try {
                                executor!!.call(borrowed)
                            } finally {
                                pool.release(borrowed)
                            }
                            return true
                        }
                        return false
                    }
                },
                completer = object : CommandCompleter {
                    override fun execute(sender: ProxyCommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>? {
                        if (completer != null) {
                            val pool = FunctionContextPool.local()
                            val borrowed = pool.borrow(completer!!, null, arrayOf(args), env)
                            try {
                                val result = completer!!.call(borrowed)
                                if (result is List<*>) {
                                    return result.filterIsInstance<String>()
                                }
                            } finally {
                                pool.release(borrowed)
                            }
                            return null
                        }
                        return null
                    }
                },
            ) {}
            // 注册可释放资源
            script.resources["command_$name"] = AutoCloseable {
                unregisterCommand(name)
                aliases.forEach { unregisterCommand(it) }
            }
        }
    }
}