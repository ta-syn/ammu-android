package com.example.ui.utils

fun toBengaliNumber(number: String): String {
    val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = java.lang.StringBuilder()
    for (char in number) {
        if (char in '0'..'9') {
            builder.append(bengaliDigits[char - '0'])
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}
