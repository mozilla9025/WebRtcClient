package app.rtcmeetings.helper

class CheckResult {

    var isValid: Boolean = false
    var formattedValue: String = ""
    var errorMessage: String? = null

    constructor(isValid: Boolean, formattedValue: String, errorMessage: String? = null) {
        this.isValid = isValid
        this.formattedValue = formattedValue
        this.errorMessage = errorMessage
    }


}
