package com.queatz.tiiight.managers

import com.queatz.tiiight.PoolMember
import com.queatz.tiiight.models.BaseModel
import com.queatz.tiiight.models.MyObjectBox
import io.objectbox.BoxStore
import kotlin.reflect.KClass

class DataManager : PoolMember() {

    private lateinit var database: BoxStore

    override fun onPoolInit() {
        database = MyObjectBox.builder().androidContext(on(ContextManager::class).context).build()
    }

    fun <T : BaseModel> box(clazz: KClass<T>) = database.boxFor(clazz.java)

}