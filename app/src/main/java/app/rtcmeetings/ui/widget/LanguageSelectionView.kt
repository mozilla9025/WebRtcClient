package app.rtcmeetings.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import app.rtcmeetings.R
import app.rtcmeetings.ui.module.MainActivity
import app.rtcmeetings.helper.dialog.DialogHelper
import app.rtcmeetings.helper.dialog.DialogInteractorCallback
import app.rtcmeetings.helper.locale.ApplicationLanguage
import app.rtcmeetings.helper.locale.LocaleManager
import app.rtcmeetings.util.logd
import kotlinx.android.synthetic.main.view_language_selection.view.*

class LanguageSelectionView : ConstraintLayout {

    @JvmOverloads
    constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
            : super(context, attributes, defStyleAttr) {

        View.inflate(context, R.layout.view_language_selection, this)

        iv_flag_cn.setOnClickListener {
            if (LocaleManager.setNewLocale(context, ApplicationLanguage.CN.languageCode)) {
                DialogHelper.showChangeLanguageRestartDialog(context, object : DialogInteractorCallback {
                    override fun onPositiveButtonClick() {
                        logd("onPositiveButtonClick: ");
                        MainActivity.start(context)
                    }

                    override fun onNegativeButtonClick() {
                    }
                })
            }
        }
        iv_flag_gb.setOnClickListener {
            if (LocaleManager.setNewLocale(context, ApplicationLanguage.EN.languageCode)) {
                DialogHelper.showChangeLanguageRestartDialog(context, object : DialogInteractorCallback {
                    override fun onPositiveButtonClick() {
                        logd("onPositiveButtonClick: ");
                        MainActivity.start(context)
                    }

                    override fun onNegativeButtonClick() {
                    }
                })
            }
        }
    }
}