#! /usr/bin/env bash

master=""
`echo -------- >> log.txt`
`date >> log.txt`
if [ $# = 1 ]; then
	if [ $1 = '-start' ]; then
		while read x; do
			echo $x
 			type=`echo $x | cut --delimiter=':' -f 1 -`
 			host=`echo $x | cut --delimiter=':' -f 3 -`
 			port=`echo $x | cut --delimiter=':' -f 5 -`
 			path=`pwd`
 			if [ $type = 'master' ]; then
 				master="$host:$port"
				echo starting master on $master
                0</dev/null ssh $host "cd $path; ./runServer.sh -s $port -master" &
                echo "master started, pid: $!"
				sleep 2s
 			else
 				echo starting slave on $host $port
                0</dev/null ssh $host "cd $path; ./runServer.sh -s $port -slave -mhost $master" &
                echo "slave started, pid: $!"
 			fi

            echo "fall off"
		done <hosts.txt
	else
		while read x; do
 			host=`echo $x | cut --delimiter=':' -f 3 -`
            echo "killing java on host $host"
    		0</dev/null ssh $host "pkill java"
		done <hosts.txt
	fi
else
	echo "Usage: ./startServers.sh [-start | -stop]"
	echo " the hosts.txt file must exists in this directory"
fi
