package kaneplarium.focustimer.model

enum class FocusDuration(val minutes: Long, val displayName: String) {
    MIN_15(15, "15 Min"),
    MIN_30(30, "30 Min"),
    MIN_45(45, "45 Min"),
    MIN_60(60, "60 Min");

    val durationMillis: Long
        get() = minutes * 60 * 1000L
}
