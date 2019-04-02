@echo off
rem java -Djava.net.useSystemProxies=true -jar seek20min.jar 1
rem
rem Arguments : 
rem * nombre d'exemplaires
rem * true=solutions incluses
rem
java -Dhttp.proxyHost=localhost -Dhttp.proxyPort=9000 -jar seek20min.jar 5 true
if not %errorlevel% == 0 goto end
del .\*_merge.pdf .\*_PAR.pdf
exit

:end
echo "Erreur %errorlevel%"
pause
