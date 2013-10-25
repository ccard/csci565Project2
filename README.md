Chris Card
Steven Ruppert
CSCI 565 Project 2: Distributed Bulletin Board
--------------------


## Files:

- Server/:
- Client/:
- lib/:
- Domain/:
- dbs/:
- runServer.java:
- runClient.java:
- startServers.java:

## Design

TODO

## Running

### Build

    make

### Running the Servers

To run a single server:

    ./runServer.sh -s <socket> [-master | -slave -mhost <master hostnem>:<master socket>]

To start a cluster of servers:

    java startServers [-start | -stop]

Note: this command reads the file called `hosts.txt` which has
lines in the following format:

    [master | slave]::<hostname>::<socket>

The first line MUST be the master server.

For example:

```
master::bb136-19.mines.edu::5555
slave::bb136-10.mines.edu::5555
slave::bb136-10.mines.edu::5556
```

## Client

TODO

## Tests

TODO
