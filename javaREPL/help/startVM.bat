cd ..\bin
java -Djline.WindowsTerminal.directConsole=false  -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address="52222" -cp  ../../libs/lib/jansi-1.16.jar;../../jline/bin/;../../libs/lib/totallylazy-2.285.jar;../../libs/lib/utterlyidle-2.120.jar;../../libs/lib/yadic-2.48.jar;.  javarepl.Main
rem java -Djline.WindowsTerminal.directConsole=false  -cp  ../../libs/lib/jansi-1.16.jar;../../jline/bin/;../../libs/lib/totallylazy-2.285.jar;../../libs/lib/utterlyidle-2.120.jar;../../libs/lib/yadic-2.48.jar;.  javarepl.Main