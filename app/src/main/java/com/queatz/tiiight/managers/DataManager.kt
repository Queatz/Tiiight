package com.queatz.tiiight.managers

import com.queatz.on.On
import com.queatz.on.OnLifecycle
import com.queatz.tiiight.models.BaseModel
import com.queatz.tiiight.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlin.reflect.KClass

class DataManager constructor(private val on: On) : OnLifecycle {

    private lateinit var database: BoxStore

    override fun on() {
        database = MyObjectBox.builder().androidContext(on<ContextManager>().context).build()
    }

    fun <T : BaseModel> box(clazz: KClass<T>): Box<T> = database.boxFor(clazz.java)

}