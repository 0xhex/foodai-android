# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces.
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# Keep all enums and their methods.
-keep enum * { public *; }

# Keep Parcelable classes and their fields.
-keep class * implements android.os.Parcelable { *; }
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep OkHttp classes.
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep SLF4J Logger classes.
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep AnnotatedType class.
-keep class java.lang.reflect.AnnotatedType { *; }
-dontwarn java.lang.reflect.AnnotatedType

# Pytorch
-dontwarn javax.annotation.Nullable
-keep class org.pytorch.** { *; }
-keep class com.facebook.jni.** { *; }
-keep class ai.onnxruntime.** { *; }

# Lokalise
-keep class com.lokalise.** { *; }
-dontwarn com.lokalise.*

# Realm
-keep @interface io.realm.annotations.RealmModule { *; }
-keep class io.realm.annotations.RealmModule { *; }

# Retain annotations needed by frameworks such as Retrofit, Room, etc.
-keepattributes *Annotation*

# Keep all public classes, methods, and fields.
-keep public class * {
    public protected *;
}

# Keep native methods.
-keepclassmembers class * {
    native <methods>;
}

# Keep Gson model classes.
-keep class com.google.gson.** { *; }

# Retrofit: keep models and Retrofit interfaces.
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.JsonSerializer

# Retrofit: keep Retrofit service interfaces.
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Ensure that enums work correctly.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Handle Lombok annotations.
-keepclassmembers class ** {
    @lombok.* <fields>;
    @lombok.* <methods>;
}

# Keep classes required by WorkManager.
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep data binding classes.
-keep class androidx.databinding.** { *; }
-dontwarn androidx.databinding.**

# Keep code used by Hilt/Dagger.
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Prevent obfuscation of logging classes.
-keep class android.util.Log {
    *;
}

# Keep classes used by Glide.
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Keep classes used by Kotlin coroutines.
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Prevent obfuscation of Timber logging classes.
-keep class timber.log.Timber {
    *;
}

# Keep classes used by your application that might be dynamically accessed.
-keep class com.yourappname.** { *; }

# R8 optimizations.
-dontoptimize
-dontshrink

# Keep lambda expressions and method references.
-keep class kotlin.jvm.functions.** { *; }
-keep class kotlin.Metadata { *; }

# Suppress warnings for any other unrecognized classes.
-dontwarn **.R
-dontwarn javax.annotation.**

# Additional dontwarn rules from missing_rules.txt
-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.api.client.http.javanet.NetHttpTransport
-dontwarn com.google.appengine.api.urlfetch.FetchOptions$Builder
-dontwarn com.google.appengine.api.urlfetch.FetchOptions
-dontwarn com.google.appengine.api.urlfetch.HTTPHeader
-dontwarn com.google.appengine.api.urlfetch.HTTPMethod
-dontwarn com.google.appengine.api.urlfetch.HTTPRequest
-dontwarn com.google.appengine.api.urlfetch.HTTPResponse
-dontwarn com.google.appengine.api.urlfetch.URLFetchService
-dontwarn com.google.appengine.api.urlfetch.URLFetchServiceFactory
-dontwarn javax.servlet.http.HttpSession
-dontwarn org.joda.time.Instant
-dontwarn com.squareup.okhttp.Call
-dontwarn com.squareup.okhttp.Callback
-dontwarn com.squareup.okhttp.Dispatcher
-dontwarn com.squareup.okhttp.Headers
-dontwarn com.squareup.okhttp.MediaType
-dontwarn com.squareup.okhttp.OkHttpClient
-dontwarn com.squareup.okhttp.Request$Builder
-dontwarn com.squareup.okhttp.Request
-dontwarn com.squareup.okhttp.RequestBody
-dontwarn com.squareup.okhttp.Response
-dontwarn com.squareup.okhttp.ResponseBody

# Keep Firebase classes.
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**

# Keep Moshi classes.
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit and Moshi: Keep classes for serialization and deserialization.
-keep class * implements com.squareup.moshi.JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit: Keep retrofit service interfaces.
-keep class * implements retrofit2.http.* { *; }
-dontwarn retrofit2.**

# Keep Timber classes
-keep class timber.log.Timber { *; }
-keep class timber.log.Timber$* { *; }
-dontwarn timber.log.Timber

# Keep your custom ReleaseTree
-keep class com.codepad.foodai.ReleaseTree { *; }
-dontwarn com.codepad.foodai.ReleaseTree