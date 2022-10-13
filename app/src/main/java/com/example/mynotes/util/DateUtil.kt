package com.example.mynotes.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    companion object {
        fun getFormattedDate(): String {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat(
                "dd/MM/yy HH:mm",
                Locale("pt", "BR")
            )
            simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT-3")
            return simpleDateFormat.format(calendar.time)
        }
    }
}