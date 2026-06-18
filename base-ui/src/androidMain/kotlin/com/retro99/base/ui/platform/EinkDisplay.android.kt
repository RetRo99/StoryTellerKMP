package com.retro99.base.ui.platform

import android.os.Build

actual fun isEinkDisplay(): Boolean {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val model = Build.MODEL.lowercase()
    val product = Build.PRODUCT.lowercase()

    val einkManufacturers = listOf(
        "onyx",
        "boox",
        "pocketbook",
        "kobo",
        "remarkable",
        "supernote",
        "bigme",
        "hisense",
        "dasung",
        "boyue",
        "likebook",
    )

    val einkModels = listOf(
        "eink",
        "e-ink",
        "epaper",
        "e-paper",
    )

    return einkManufacturers.any { manufacturer.contains(it) } ||
        einkModels.any { model.contains(it) || product.contains(it) }
}
