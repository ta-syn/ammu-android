package com.example.ui.utils

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareApp(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "আম্মু অ্যাপ - বাংলাদেশি মায়েদের AI সহকারী")
            putExtra(
                Intent.EXTRA_TEXT,
                "আম্মু অ্যাপটি ব্যবহার করে দেখুন! নামাজের সময়, কোরআন, হাদিস, স্বাস্থ্য তথ্য, এবং AI চ্যাট — সবকিছু এখন বাংলায়। আপনার প্রতিদিনের নির্ভরযোগ্য সহকারী।\n\nডাউনলোড করুন: https://play.google.com/store/apps/details?id=com.aistudio.ammu"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
    }
}
