package com.greenrou.rouxen.feature.traffic.data

object OuiVendorLookup {

    fun lookup(macAddress: String): String? {
        val oui = macAddress.replace(":", "").replace("-", "").uppercase()
        if (oui.length < 6) return null
        return TABLE[oui.substring(0, 6)]
    }

    private val TABLE: Map<String, String> = mapOf(
        "F0DBE2" to "Apple",
        "AC87A3" to "Apple",
        "DCA4CA" to "Apple",
        "3C15C2" to "Apple",
        "F01898" to "Apple",
        "40B0FA" to "Apple",
        "5C0A5B" to "Samsung",
        "8C71F8" to "Samsung",
        "3423BA" to "Samsung",
        "CC07AB" to "Samsung",
        "E8508B" to "Samsung",
        "286C07" to "Xiaomi",
        "640980" to "Xiaomi",
        "F8A45F" to "Xiaomi",
        "34CE00" to "Xiaomi",
        "546009" to "Google",
        "F4F5D8" to "Google",
        "A47733" to "Google",
        "240AC4" to "Espressif",
        "30AEA4" to "Espressif",
        "3C71BF" to "Espressif",
        "A4CF12" to "Espressif",
        "CC50E3" to "Espressif",
        "ECFABC" to "Espressif",
        "50C7BF" to "TP-Link",
        "A42BB0" to "TP-Link",
        "EC086B" to "TP-Link",
        "B04E26" to "TP-Link",
        "00E0FC" to "Huawei",
        "4846FB" to "Huawei",
        "D46E5C" to "Huawei",
        "74C246" to "Amazon",
        "40B4CD" to "Amazon",
        "FC65DE" to "Amazon",
        "B827EB" to "Raspberry Pi",
        "DCA632" to "Raspberry Pi",
        "E45F01" to "Raspberry Pi",
        "000E58" to "Sonos",
        "5CAAFD" to "Sonos",
        "001B21" to "Intel",
        "A0A8CD" to "Intel",
        "0050F2" to "Microsoft",
        "7CBB8A" to "Nintendo",
        "98B6E9" to "Nintendo",
        "B0A737" to "Roku",
        "D83134" to "Roku",
        "A039F7" to "LG",
        "C4366C" to "LG",
    )
}
