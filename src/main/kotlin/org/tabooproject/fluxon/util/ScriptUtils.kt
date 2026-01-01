package org.tabooproject.fluxon.util

import org.tabooproject.fluxon.FluxonScript
import org.tabooproject.fluxon.runtime.Environment
import org.tabooproject.fluxon.runtime.Function
import org.tabooproject.fluxon.runtime.FunctionContextPool

fun Environment.getFluxonScript(): FluxonScript? {
    return rootVariables["__script__"] as? FluxonScript
}

fun Function.invoke(env: Environment, args: Array<Any?>, target: Any?): Any? {
    val pool = FunctionContextPool.local()
    val borrowed = pool.borrow(this, target, args, env)
    return try {
        call(borrowed)
    } finally {
        pool.release(borrowed)
    }
}

fun Function.invokeInline(env: Environment, count: Int, args0: Any?, args1: Any?, args2: Any?, args3: Any?, target: Any?): Any? {
    val pool = FunctionContextPool.local()
    val borrowed = pool.borrowInline(this, target, count, args0, args1, args2, args3, env)
    return try {
        call(borrowed)
    } finally {
        pool.release(borrowed)
    }
}