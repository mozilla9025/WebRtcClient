package app.rtcmeetings.ui.widget

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import app.rtcmeetings.R
import app.rtcmeetings.util.initForPhoneInput
import kotlinx.android.synthetic.main.view_register_form_input.view.*

class RegisterFormInputView : ConstraintLayout {

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_PHONE = 1
        const val TYPE_PASSWORD = 2
        const val TYPE_EMAIL = 3
    }

    @JvmOverloads
    constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
            : super(context, attributes, defStyleAttr) {

        View.inflate(context, R.layout.view_register_form_input, this)
        initListeners()
        attributes?.let { handleAttrs(context, attributes, defStyleAttr) }
    }

    var text: String
        get() = edit_text.text.toString()
        set(text) = edit_text.setText(text)

    var phone: String
        get() = edit_text_phone.text.toString()
        set(text) = edit_text_phone.setText(text)


    private fun initListeners() {
        edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setError(null)
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        edit_text_phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setError(null)
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun handleAttrs(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.theme.obtainStyledAttributes(attrs,
                R.styleable.RegisterFormInputView, defStyleAttr, 0)

        try {
            val text = a.getString(R.styleable.RegisterFormInputView_rfiv_text)
            val hint = a.getString(R.styleable.RegisterFormInputView_rfiv_hint)
            val title = a.getString(R.styleable.RegisterFormInputView_rfiv_title)
            val type = a.getInteger(R.styleable.RegisterFormInputView_rfiv_type, TYPE_TEXT)
            val maxLines = a.getInteger(R.styleable.RegisterFormInputView_rfiv_maxLines, 1)

            tv_title.text = title
            edit_text.setText(text)
            edit_text.hint = hint
            edit_text.maxLines = maxLines
            edit_text_phone.hint = hint

            when (type) {
                TYPE_TEXT -> {
                    text_input_layout.visibility = View.VISIBLE
                    edit_text_phone.visibility = View.GONE

                    edit_text.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                }
                TYPE_PASSWORD -> {
                    text_input_layout.visibility = View.VISIBLE
                    edit_text_phone.visibility = View.GONE
                }
                TYPE_PHONE -> {
                    text_input_layout.visibility = View.GONE
                    edit_text_phone.visibility = View.VISIBLE

                    edit_text_phone.initForPhoneInput()
                }
                TYPE_EMAIL -> {
                    text_input_layout.visibility = View.VISIBLE
                    edit_text_phone.visibility = View.GONE

                    edit_text.inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }
            }
        } finally {
            a.recycle()
        }
    }

    fun setError(errorText: String?) {
        if (!TextUtils.isEmpty(errorText)) {
            if (tv_error.visibility != View.VISIBLE)
                tv_error.visibility = View.VISIBLE
            if (!edit_text.isSelected)
                edit_text.isSelected = true
            if (!edit_text_phone.isSelected)
                edit_text_phone.isSelected = true

            tv_error.text = errorText
        } else {
            tv_error.text = null
            if (tv_error.visibility != View.GONE)
                tv_error.visibility = View.GONE
            if (edit_text.isSelected)
                edit_text.isSelected = false
            if (edit_text_phone.isSelected)
                edit_text_phone.isSelected = false
        }
    }
}