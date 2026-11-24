# Retrofit / Gson models are serialised reflectively.
-keep class com.vastra.store.data.model.** { *; }
-keep class com.vastra.store.data.local.** { *; }
-keep class com.vastra.store.ui.SortOption { *; }

-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes *Annotation*

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp / Retrofit platform warnings
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
