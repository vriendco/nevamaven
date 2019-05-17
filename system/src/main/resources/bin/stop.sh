#Monitor:
#Execute using the command 
#./startServer.h &
#The pid is maintened into PID file (see below)

echo -e "STOPING NEVAMAVEN SERVER"

#variables
PID_FILE=/server/cache/serverMaven.pid  

kill -9 `cat $PID_FILE`

rm -r -f $PID_FILE