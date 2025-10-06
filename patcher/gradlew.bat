@echo off
setlocal

set DIR=%~dp0
set APP_HOME=%DIR%

set DEFAULT_JVM_OPTS=

if defined JAVA_HOME (
    set JAVA_EXEC=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXEC=java.exe
)

"%JAVA_EXEC%" %DEFAULT_JVM_OPTS% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
