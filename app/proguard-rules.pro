# Reflex Avı — R8 / ProGuard rules
#
# Note: Firestore access in LeaderboardRepository reads fields manually
# (document.getString / getLong). No reflection-based POJO mapping (toObject) is used,
# so the model classes need no keep rules. If that changes — i.e. toObject/@PropertyName
# start being used — the affected model classes must be kept explicitly.

# --- Crashlytics: readable stack traces ---
# Without the source file and line number attributes, Crashlytics reports come back
# obfuscated and cannot be traced.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-keepattributes *Annotation*
-dontwarn kotlinx.coroutines.**

# --- Google Mobile Ads (AdMob) ---
# The AAR ships its own consumer rules; we only silence warnings here.
-dontwarn com.google.android.gms.**

# --- Firebase ---
-dontwarn com.google.firebase.**
