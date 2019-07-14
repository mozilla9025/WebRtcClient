package app.rtcmeetings.util

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import app.rtcmeetings.helper.locale.ApplicationLanguage


fun View.openKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, 0)
}

fun View.closeKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.initForPhoneInput() {

    inputType = InputType.TYPE_CLASS_PHONE
    addTextChangedListener(PhoneNumberFormattingTextWatcher(ApplicationLanguage.CN.languageCode))

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        @SuppressLint("SetTextI18n")
        override fun afterTextChanged(s: Editable?) {
            val text = text ?: return

            if (text.isEmpty() or text.startsWith("+86")) return

            if (text.startsWith("+")) {
                if (text.startsWith("+8")) {
                    if (!text.startsWith("+86") && text.length > 2) {
                        setText("+86${text.substring(2, text.length)}")
                    }
                } else {
                    if (text.length > 1) {
                        setText("+8${text.substring(1, text.length)}")
                    }
                }
            } else {
                if (text.startsWith("86") || text.startsWith("8")) {
                    setText("+$text")
                } else {
                    setText("+86$text")
                }
            }
            setSelection(getText().length)
            setError(null)
        }
    })
}