@ECHO OFF

SET DIRNAME=%~dp0
SET APP_BASE_NAME=%~n0

SET CLASSPATH=%DIRNAME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% ^
-classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*