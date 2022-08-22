package com.bakaikin.sergey.reminder.adapter

import android.animation.Animator
import com.bakaikin.sergey.reminder.fragment.DoneTaskFragment
import com.bakaikin.sergey.reminder.adapter.TaskAdapter
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import com.bakaikin.sergey.reminder.R
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import com.bakaikin.sergey.reminder.adapter.TaskAdapter.TaskViewHolder
import com.bakaikin.sergey.reminder.model.ModelTask
import android.view.View.OnLongClickListener
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.os.Handler
import android.view.View
import com.bakaikin.sergey.reminder.Utils

/**
 * Created by Sergey on 19.09.2015.
 */
class DoneTaskAdapter(taskFragment: DoneTaskFragment?) : TaskAdapter(taskFragment) {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.model_task, viewGroup, false)
        val title = v.findViewById<View>(R.id.tvTaskTitle) as TextView
        val date = v.findViewById<View>(R.id.tvTaskDate) as TextView
        val priority = v.findViewById<View>(R.id.cvTaskPriority) as CircleImageView
        return TaskViewHolder(v, title, date, priority)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (item.isTask) {
            viewHolder.itemView.isEnabled = true
            val task = item as ModelTask
            val taskViewHolder = viewHolder as TaskViewHolder
            val itemView = taskViewHolder.itemView
            val resources = itemView.resources
            taskViewHolder.title.text = task.title
            if (task.date != 0L) {
                taskViewHolder.date.text = Utils.getFullDate(task.date)
            } else {
                taskViewHolder.date.text = null
            }
            itemView.visibility = View.VISIBLE
            taskViewHolder.priority.isEnabled = true
            taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_disabled_material_light))
            taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_disabled_material_light))
            taskViewHolder.priority.setColorFilter(resources.getColor(task.priorityColor))
            taskViewHolder.priority.setImageResource(R.drawable.ic_check_circle_white_48dp)
            itemView.setOnLongClickListener {
                val handler = Handler()
                handler.postDelayed(
                    { getTaskFragment().removeTaskDialog(taskViewHolder.layoutPosition) },
                    1000
                )
                true
            }
            taskViewHolder.priority.setOnClickListener {
                taskViewHolder.priority.isEnabled = false
                task.status = ModelTask.STATUS_CURRENT
                getTaskFragment().activity.dbHelper.update()
                    .status(task.timeStamp, ModelTask.STATUS_CURRENT)
                taskViewHolder.title.setTextColor(resources.getColor(R.color.primary_text_default_material_light))
                taskViewHolder.date.setTextColor(resources.getColor(R.color.secondary_text_default_material_light))
                taskViewHolder.priority.setColorFilter(resources.getColor(task.priorityColor))
                val flipIn = ObjectAnimator.ofFloat(taskViewHolder.priority, "rotationY", 180f, 0f)
                taskViewHolder.priority.setImageResource(R.drawable.ic_checkbox_blank_circle_white_48dp)
                flipIn.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        if (task.status != ModelTask.STATUS_DONE) {
                            val translationX = ObjectAnimator.ofFloat(
                                itemView,
                                "translationX", 0f, -itemView.width.toFloat()
                            )
                            val translationXBack = ObjectAnimator.ofFloat(
                                itemView,
                                "translationX", -itemView.width.toFloat(), 0f
                            )
                            translationX.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {}
                                override fun onAnimationEnd(animation: Animator) {
                                    itemView.visibility = View.GONE
                                    getTaskFragment().moveTask(task)
                                    removeItem(taskViewHolder.layoutPosition)
                                }

                                override fun onAnimationCancel(animation: Animator) {}
                                override fun onAnimationRepeat(animation: Animator) {}
                            })
                            val translationSet = AnimatorSet()
                            translationSet.play(translationX).before(translationXBack)
                            translationSet.start()
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                flipIn.start()
            }
        }
    }
}