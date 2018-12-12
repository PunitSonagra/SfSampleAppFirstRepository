-keep class org.apache.http.** { *; }
 -keepclassmembers class org.apache.http.** {*;}
 -dontwarn org.apache.**
 -keep class android.net.http.** { *; }
 -keepclassmembers class android.net.http.** {*;}
 -dontwarn android.net.**
 -keep class cn.pedant.SweetAlert.Rotate3dAnimation {
       public <init>(...);
    }