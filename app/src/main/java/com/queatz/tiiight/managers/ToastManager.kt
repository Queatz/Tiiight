package com.queatz.tiiight.managers

import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
import com.queatz.on.On

class ToastManager constructor(private val on: On) {
    fun show(@StringRes resId: Int) {
        Toast.makeText(on<ContextManager>().context, resId, LENGTH_SHORT).show()
    }

    fun show(string: String) {
        Toast.makeText(on<ContextManager>().context, string, LENGTH_SHORT).show()
    }
}