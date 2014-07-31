# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Program Files (x86)/Android/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

#Warnings to be removed. Otherwise maven plugin stops, but not dangerous
-dontwarn android.support.**
-dontwarn com.sun.xml.internal.**
-dontwarn com.sun.istack.internal.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.springframework.**
-dontwarn java.awt.**
-dontwarn javax.security.**
-dontwarn java.beans.**
-dontwarn javax.xml.**
-dontwarn java.util.**
-dontwarn org.w3c.dom.**
-dontwarn com.google.common.**
-dontwarn com.octo.android.robospice.persistence.**

### For Android, disable Log output
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
