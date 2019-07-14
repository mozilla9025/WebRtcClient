package app.rtcmeetings.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.rtcmeetings.R
import kotlinx.android.synthetic.main.view_toolbar.view.*

class ToolbarView @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attributes, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_toolbar, this)
        val a = context.theme.obtainStyledAttributes(attributes, R.styleable.ToolbarView, defStyleAttr, 0)
        try {
            if (a.hasValue(R.styleable.ToolbarView_tv_action_left_icon)) {
                val leftIcon = a.getDrawable(R.styleable.ToolbarView_tv_action_left_icon)
                setActionLeftIcon(leftIcon)
                ib_action_left.visibility = View.VISIBLE
            } else {
                ib_action_left.visibility = View.GONE
            }
            val leftIconTint = a.getColor(R.styleable.ToolbarView_tv_action_left_tint, ContextCompat.getColor(context, R.color.textColor))
            setActionLeftIconTint(leftIconTint)

            if (a.hasValue(R.styleable.ToolbarView_tv_action_icon)) {
                val actionIcon = a.getDrawable(R.styleable.ToolbarView_tv_action_icon)
                setActionIcon(actionIcon)
                ib_action.visibility = View.VISIBLE
            } else {
                ib_action.visibility = View.GONE
            }

            val actionIconTint = a.getColor(R.styleable.ToolbarView_tv_action_tint, ContextCompat.getColor(context, R.color.textColor))
            setActionIconTint(actionIconTint)

            val text = a.getString(R.styleable.ToolbarView_tv_text)
            setText(text)

            val textColor = a.getColor(R.styleable.ToolbarView_tv_text_color, ContextCompat.getColor(context, R.color.textColorDark))
            setTextColor(textColor)

        } finally {
            a.recycle()
        }
    }

    fun setActionLeftIcon(icon: Drawable?) {
        ib_action_left.setImageDrawable(icon)
    }

    fun setActionLeftIcon(@DrawableRes icon: Int) {
        ib_action_left.setImageResource(icon)
    }

    fun setActionLeftIconTint(color: Int) {
        ib_action_left.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun setActionIcon(icon: Drawable?) {
        ib_action.setImageDrawable(icon)
    }

    fun setActionIcon(@DrawableRes icon: Int) {
        ib_action.setImageResource(icon)
    }

    fun setActionIconTint(color: Int) {
        ib_action.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun setText(text: String) {
        tv_title.text = text
    }

    fun setText(@StringRes text: Int) {
        tv_title.setText(text)
    }

    fun setTextColor(color: Int) {
        tv_title.setTextColor(color)
    }

    fun setOnActionClickListener(onClickListener: () -> Unit) {
        ib_action.setOnClickListener { onClickListener.invoke() }
    }
    fun setOnActionLeftClickListener(onClickListener: () -> Unit) {
        ib_action_left.setOnClickListener { onClickListener.invoke() }
    }
}