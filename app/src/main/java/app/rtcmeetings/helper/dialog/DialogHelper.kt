package app.rtcmeetings.helper.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import app.rtcmeetings.R
import app.rtcmeetings.util.logd
import kotlinx.android.synthetic.main.dialog_find_contact.view.*

class DialogHelper {

    companion object {
        fun showChangeLanguageRestartDialog(from: Context, callback: DialogInteractorCallback) {
            AlertDialog.Builder(from)
                    .setCancelable(false)
                    .setTitle("Change language")
                    .setMessage("Changing language requires restarting of the application")
                    .setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int ->
                        logd("showChangeLanguageRestartDialog: ")
                        dialogInterface.dismiss()
                        callback.onPositiveButtonClick()
                    }
                    .create()
                    .show()
        }
    }
}