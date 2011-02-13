@echo off

REM Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
REM "path:path:..."
REM 
REM
SET JDBCPATH=C:\path\to\jdbc\driver.jar

SET CP=%JDBCPATH%;isatools_deps.jar

java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m\
     -cp %CP% org.isatools.gui.datamanager.DataManagerInvoker
