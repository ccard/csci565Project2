#! /usr/bin/env bash

master=""
if [ $# = 1 ]; then
	if [ $1 = '-start' ]; then
		while read x; do
 			type=`echo $x | cut --delimiter=':' -f 1 -`
 			host=`echo $x | cut --delimiter=':' -f 3 -`
 			port=`echo $x | cut --delimiter=':' -f 5 -`
 			path=`pwd`
 			if [ $type = 'master' ]; then
 				master="$host:$port"
    			echo `ssh $host "cd $path; ./runServer.sh -s $port -master"`
 			else
				echo `ssh $host "cd $path; ./runServer.sh -s $port -slave -mhost $master"`
 			fi
		done <hosts.txt
	else
		while read x; do
 			host=`echo $x | cut --delimiter=':' -f 3 -`
    		echo `ssh $host "pkill java"`
		done <hosts.txt
	fi
else
	echo "Usage: ./startServers.sh [-start | -stop]"
	echo " the hosts.txt file must exists in this directory"
fi
