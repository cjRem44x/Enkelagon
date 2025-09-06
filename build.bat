@echo off
set proj=%cd%
set bin=%proj%\bin
set lib=%proj%\lib
set src=%proj%\src

:: Java Specs
set java_APP_args=a b c d
set java_TEST_args=
set java_APP_pkg_paths=*.java
set java_TEST_pkg_paths=*.java

:: JAVA
set java=%src%\java

:: Set classpath: bin + all jars in lib
set classpath=%bin%\java;%lib%\*

:: compile java app
cd "%java%\app"
javac -cp "%classpath%" -d "%bin%\java" %java_APP_pkg_paths%

:: compile java test
cd "%java%\test"
javac -cp "%classpath%" -d "%bin%\java" %java_TEST_pkg_paths%

:: run applications
cd "%bin%\java"
java -cp "%classpath%" app.Main %java_APP_args%
java -cp "%classpath%" test.MainTest %java_TEST_args%

cd "%proj%"
pause

