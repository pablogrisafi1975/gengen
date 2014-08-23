cls
del c:\temp\gengen.log
cd gengen
call mvn clean install
cd..
cd gengendemo
call mvn -e clean install
cd..
