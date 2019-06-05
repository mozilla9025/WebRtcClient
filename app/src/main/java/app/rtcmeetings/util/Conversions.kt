package app.rtcmeetings.util

inline val Int.l get() = this.toLong()

inline val Float.l get() = this.toLong()

inline val Double.l get() = this.toLong()

inline val Int.f get() = this.toFloat()

inline val Long.f get() = this.toFloat()

inline val Double.f get() = this.toFloat()

inline val Int.d get() = this.toDouble()

inline val Float.d get() = this.toDouble()

inline val Long.d get() = this.toDouble()

inline val String.d get() = this.toDouble()

inline val String.i get() = this.toInt()

inline val String.l get() = this.toLong()

inline val String.f get() = this.toFloat()
