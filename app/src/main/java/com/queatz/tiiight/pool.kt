package com.queatz.tiiight

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class Pool {

    private val members = HashMap<Class<*>, PoolMember>()

    fun <T : PoolMember> on(member: KClass<T>): T {
        if (!members.containsKey(member.java)) {
            try {
                members[member.java] = member.java.getConstructor().newInstance().setPool(this)
            } catch (e: ReflectiveOperationException) {
                e.printStackTrace()
            }

        }

        return members[member.java] as T
    }

    fun end() {
        for (member in members.values) {
            member.onPoolEnd()
        }
    }

    companion object {
        val scopes = HashMap<Any, Pool>()
    }
}

open class PoolMember {

    private lateinit var pool: Pool

    fun setPool(pool: Pool): PoolMember {
        this.pool = pool
        onPoolInit()
        return this
    }

    open fun onPoolInit() {}
    open fun onPoolEnd() {}

    fun <T : PoolMember> on(member: KClass<T>): T {
        return pool.on(member)
    }
}


fun <T : PoolMember> Any.on(member: KClass<T>): T {
    if (!Pool.scopes.containsKey(this)) {
        Pool.scopes[this] = Pool()
    }

    return Pool.scopes[this]!!.on(member)
}

fun <T : PoolMember> Any.on(member: KClass<T>, apply: (member: T) -> Unit) {
    apply.invoke(on(member).on(member))
}

fun Any.onEnd() = Pool.scopes[this]?.let {
    it.end()
    Pool.scopes.remove(it)
}