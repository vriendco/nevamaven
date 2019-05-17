#Monitor:
#Execute using the command 
#./startServer.h &
#The pid is maintened into PID file (see below)


cd ../

#variables
CONTINUE="TRUE"
PID_FILE=/server/cache/serverMaven.pid 

if [ -e "$PID_FILE" ] ; then
	echo -e "Previous instance of monitor founded, shutdown before or remove the $PID_FILE process."
	exit -1
fi

echo -e "STARTING NEVAMAVEN SERVER"
date

rm -r -f /server/logs/serverMaven.log
cd /server/scripts/serverMaven

java -cp "/server/scripts/serverMaven/lib/*:." com.vriend.nevamaven.server.MainNServer "-p" "7777" "-l" "/server/repository/" "-r" "http://central.maven.org/maven2" "-v">>/server/logs/serverMaven.log &

echo $!>"$PID_FILE"

