package com.bakaikin.sergey.reminder.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.bakaikin.sergey.reminder.R
import com.bakaikin.sergey.reminder.Utils
import com.bakaikin.sergey.reminder.alarm.AlarmHelper
import com.bakaikin.sergey.reminder.model.ModelTask
import com.google.android.material.textfield.TextInputLayout
import java.util.*

/**
 * Created by Sergey on 19.09.2015.
 */
class AddingTaskDialogFragment : DialogFragment() {
    private var addingTaskListener: AddingTaskListener? = null

    interface AddingTaskListener {
        fun onTaskAdded(newTask: ModelTask?)
        fun onTaskAddingCancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        addingTaskListener = try {
            activity as AddingTaskListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement AddingTaskListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_title)
        val inflater = activity?.layoutInflater
        val container = inflater?.inflate(R.layout.dialog_task, null)
        val tilTitle = container?.findViewById<View>(R.id.tilDialogTaskTitle) as TextInputLayout
        val etTitle = tilTitle.editText
        val tilDate = container.findViewById<View>(R.id.tilDialogTaskDate) as TextInputLayout
        val etDate = tilDate.editText
        val tilTime = container.findViewById<View>(R.id.tilDialogTaskTime) as TextInputLayout
        val etTime = tilTime.editText
        val spPriority = container.findViewById<View>(R.id.spDialogTaskPriority) as Spinner
        tilTitle.hint = resources.getString(R.string.task_title)
        tilDate.hint = resources.getString(R.string.task_date)
        tilTime.hint = resources.getString(R.string.task_time)
        builder.setView(container)
        val task = ModelTask()
        val priorityAdapter = activity?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.priority_levels)
            )
        }
        spPriority.adapter = priorityAdapter
        spPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                task.priority = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = calendar[Calendar.HOUR_OF_DAY] + 1
        etDate!!.setOnClickListener {
            if (etDate.length() == 0) {
                etDate.setText(" ")
            }
            val datePickerFragment: DatePickerFragment = object : DatePickerFragment() {
                override fun onDateSet(
                    view: DatePicker,
                    year: Int,
                    monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    etDate.setText(Utils.getDate(calendar.timeInMillis))
                }

                override fun onCancel(dialog: DialogInterface) {
                    etDate.text = null
                }
            }
            fragmentManager?.let { it1 -> datePickerFragment.show(it1, "DatePickerFragment") }
        }
        etTime!!.setOnClickListener {
            if (etTime.length() == 0) {
                etTime.setText(" ")
            }
            val timePickerFragment: DialogFragment = object : TimePickerFragment() {
                override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
                    calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                    calendar[Calendar.MINUTE] = minute
                    calendar[Calendar.SECOND] = 0
                    etTime.setText(Utils.getTime(calendar.timeInMillis))
                }

                override fun onCancel(dialog: DialogInterface) {
                    etTime.text = null
                }
            }
            activity?.supportFragmentManager?.let { it1 -> timePickerFragment.show(it1, "TimePickerFragment") }
        }
        builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
            task.title = etTitle!!.text.toString()
            task.status = ModelTask.STATUS_CURRENT
            if (etDate.length() != 0 || etTime.length() != 0) {
                task.date = calendar.timeInMillis
                val alarmHelper = AlarmHelper.getInstance()
                alarmHelper.setAlarm(task)
            }
            task.status = ModelTask.STATUS_CURRENT
            addingTaskListener!!.onTaskAdded(task)
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
            addingTaskListener!!.onTaskAddingCancel()
            dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.setOnShowListener { dialog ->
            val positiveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            if (etTitle!!.length() == 0) {
                positiveButton.isEnabled = false
                tilTitle.error = resources.getString(R.string.dialog_error_empty_title)
            }
            etTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.length == 0) {
                        positiveButton.isEnabled = false
                        tilTitle.error = resources.getString(R.string.dialog_error_empty_title)
                    } else {
                        positiveButton.isEnabled = true
                        tilTitle.isErrorEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
        }
        return alertDialog
    }
}