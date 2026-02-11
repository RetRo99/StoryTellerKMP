package org.retro99.storyteller

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    /**
     * Detects if the device is an e-ink device by checking manufacturer/model.
     * Known e-ink device manufacturers include BOOX, Onyx, PocketBook, etc.
     */
    override val isEink: Boolean = isEinkDevice()

    private fun isEinkDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()

        // Known e-ink device manufacturers and models
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
}

actual fun getPlatform(): Platform = AndroidPlatform()