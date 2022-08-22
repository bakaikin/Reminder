package com.bakaikin.sergey.reminder.dialog

import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.bakaikin.sergey.reminder.Utils
import java.util.*

/**
 * Created by Sergey on 19.09.2015.
 */
open class DatePickerFragment : DialogFragment(), OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {}
}

