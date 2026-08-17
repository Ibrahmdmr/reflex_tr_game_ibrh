# Firestore models need no keep rules: LeaderboardRepository reads fields manually, never
# via toObject/@PropertyName. Add keeps here if that changes.

# --- Crashlytics: readable stack traces ---
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
