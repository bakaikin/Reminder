package com.bakaikin.sergey.reminder.dialog

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        etDate!!.setOnClickListener {
            if (etDate.length() == 0) {
                etDate.setText(" ")
            }

            DatePickerDialog(
                requireActivity(),
                { view, year, month, day ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = month
                    calendar[Calendar.DAY_OF_MONTH] = day
                    etDate.setText(Utils.getDate(calendar.timeInMillis))
                }, year, month, day
            ).show()
        }

        etTime!!.setOnClickListener {
            if (etTime.length() == 0) {
                etTime.setText(" ")
            }

            TimePickerDialog(requireActivity(),
                TimePickerDialog.OnTimeSetListener { view, hour, minute ->
                    calendar[Calendar.HOUR_OF_DAY] = hour
                    calendar[Calendar.MINUTE] = minute
                    calendar[Calendar.SECOND] = 0
                    etTime.setText(Utils.getTime(calendar.timeInMillis))
                }, hour, minute,true
            ).show()

//            val timePickerFragment: DialogFragment = object : TimePickerFragment() {
//                override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
//                    calendar[Calendar.HOUR_OF_DAY] = hourOfDay
//                    calendar[Calendar.MINUTE] = minute
//                    calendar[Calendar.SECOND] = 0
//                    etTime.setText(Utils.getTime(calendar.timeInMillis))
//                }
//
//                override fun onCancel(dialog: DialogInterface) {
//                    etTime.text = null
//                }
//            }
//            timePickerFragment.show(parentFragmentManager, "TimePickerFragment")
////            activity?.supportFragmentManager?.let { it1 -> timePickerFragment.show(it1, "TimePickerFragment") }
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