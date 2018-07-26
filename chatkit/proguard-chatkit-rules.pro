#
## JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
#
## A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
#
## Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
#
## OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
#
#### OKHTTP
#
## Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote okhttp3.internal.Platform
#
#
#### OKIO
#
## java.nio.file.* usage which cannot be used at runtime. Animal sniffer annotation.
-dontwarn okio.Okio
## JDK 7-only method which is @hide on Android. Animal sniffer annotation.
-dontwarn okio.DeflaterSink

-dontwarn okhttp3.**
#
##### -- Picasso --
 -dontwarn com.squareup.picasso.**
#
# #### -- OkHttp --
#
 -dontwarn com.squareup.okhttp.internal.**
#
# #### -- Apache Commons --
#
 -dontwarn org.apache.commons.logging.**
#
-dontwarn org.xmlpull.v1.**