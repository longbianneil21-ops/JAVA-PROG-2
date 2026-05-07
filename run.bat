@echo off
echo Compiling...
javac --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls -cp "src;C:\javafx-sdk\lib\mysql-connector-j-9.7.0.jar" src\Main.java src\LoginScreen.java src\RegisterScreen.java src\ChatScreen.java src\Chatbot.java src\Database.java src\Config.java src\SchoolData.java -d out

echo Running...
java --module-path "C:\javafx-sdk\lib" --add-modules javafx.controls -cp "out;C:\javafx-sdk\lib\mysql-connector-j-9.7.0.jar" Main
pause