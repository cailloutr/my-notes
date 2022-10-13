package com.example.mynotes.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    // TODO: fix: time is in the wrong time-zone
    companion object {
        fun getFormattedDate(): String {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat(
                "dd/MM/yy HH:mm",
                Locale("pt", "BR")
            )

            return simpleDateFormat.format(calendar.time)
        }
    }
}