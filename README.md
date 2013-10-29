Chris Card
Steven Ruppert
CSCI 565 Project 2: Distributed Bulletin Board
--------------------


## Files:

- Server/:

 - *ArticleStore.java*: This is the local database for each server and is also used for some
concurrent access controls

 - *Server.java*: This starts the specific types of servers either master or slave and binds
them to a port

 - *MasterServer.java*: This contains the code specific to the *master/cordinator* node that controlls
the *read/write* quorums to ensure the eventual consistency of the system. The master can be contacted
directly by the client but the client shouldn't know if they are contacting the master or not.

 - *SlaveServer.java*: This contains the code specific to the *slaveservers*  node that responds
 to masters quorum request both read and write as well as servering requests for articles.

 - *Slave.java*: This class is an interface for the slave and node class to define general sever
 specific methods like replicate write and getLocalArticle methods.

 - *Master.java*: This class is an interface for Master specific functionality like regestering
 the slave nodes

 - *Node.java*: This class implements the slave interface and the is code for the general server
 functionality

- Client/:

 - *Client.java*: This class is used primaraly for the client interactions with the servers. i.e.
 posting an article asking for an article or a list of articles

- lib/:

 - *guava-15.0.jar*: This provides a thread executor service allowing for creating a thread poll to
 run multiple threads simultaneously

 - *jdbi-2.5.1.jar*: this library provides or data base for storing articles and also provides some
 unique id production as well as some concurrent access control

 - *log4j-api-2.0-beta9.jar*: This provides logging for the servers and client to facilitate debugging
 and operation of the servers

 - *log4j-core-2.0-beta9.jar*: This does the same thing as its counter part above

- Domain/:

 - *Article.java*: This class is used to define articles and exchagne them between the
 servers and the clients

 - *BulletinBoard.java*: This class is the overall interface for our java rmi methods and defines
 the three basic functions of the servers: post, choose, list

- *runServer.sh*: This script starts a server either a master or slave

- *runClient.sh*: This script starts the client and allows for interaction with the client

- *startServers.sh*: This script starts the servers on multiple machines and reads in the machines
to start on from the hosts.txt file

- *hosts.txt*: defines the machines, ports, and types of servers to start on each machine
and is in the following formate
    type::hostname::portnumber
ex:
    [master | slave]::bb136-19.mines.edu::5555

- *testClientMethods.java*: This class is used for testing the system through client interaction with
several different servers

## Design

TODO

## Running

### Build

    make

### Running the Servers

To run a single server:

    ./runServer.sh -s <socket> [-master | -slave -mhost <master hostname>:<master socket>]

To start a cluster of servers:

    ./startServers.sh [-start | -stop]

**Note**: this command reads the file called `hosts.txt` which has
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

To interact with the server:

    ./client.sh [options]

Run `client.sh` without options for documentation and example usage.

## Tests

### Build

    make

### Run Tests

    java testClientMethods

This will print out success if the tests pass other wise it will throw an error

### Benchmark
-To run the bench mark:

