package com.queatz.tiiight

import android.app.Service
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.showKeyboard(show: Boolean) {
    val inputMethodManager = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager

    when (show) {
        true -> inputMethodManager.showSoftInput(this, 0)
        false -> inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.GONE }
