# Keep CH34X driver classes
-keep class com.ch34x.usbtool.driver.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep GSON
-keep class com.google.gson.** { *; }
-keep class com.ch34x.usbtool.flash.FlashDatabase$FlashInfo { *; }
-keep class com.ch34x.usbtool.flash.FlashDatabase$InstructionSet { *; }

# Keep application classes
-keep class com.ch34x.usbtool.** { *; }

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}