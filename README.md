# CSCI 565 Project 2: Distributed Bulletin Board

Authors: Chris Card, Steven Ruppert

## Files:

- Server/:
 - *Server.java*: starts the specific types of servers either master or slave
    and binds them to a port.
 - *MasterServer.java*: The master/coordinator node implementation. Controls the read/write
   quorums to ensure the eventual consistency of the system.
 - *SlaveServer.java*: Slave node implementation. Forwards operations to the master, and
   replicates writes and reads.
 - *Slave.java*: Interface definition for slave nodes, i.e. local read/write operations.
 - *Master.java*: Master/coordinator-specific tasks e.g. slave node registration.
 - *Node.java*: `ArticleStore`-backed implementation of BulletinBoard. Both MasterServer and
    SlaveServer share this superclass.
- Client/:
 - *Client.java*: CLI client.
- lib/: third-party libraries used in the project.
 - *guava-15.0.jar*: Various nice java library functions.
 - *jdbi-2.5.1.jar*: JDBC database access control made easier.
 - *log4j-api-2.0-beta9.jar, log4j-core-2.0-beta9.jar*: Log4j2 logging framework.
 - *jcommander-1.32.jar*: args parsing without ripping your hair out.
- Domain/:
 - *Article.java*: Main domain class, i.e. has an id and some text content.
 - *BulletinBoard.java*: Client-facing interface for server nodes, i.e. POST, LIST, and GET
   operations.
- Shared/:
 - *ArticleStore.java*: JDBI-based persistent article database DAO.
- *runServer.sh*: Starts a server either a master or slave
- *client.sh*: Convenience script to run CLI client defined in `Client.java` with the
  correct java RMI options.
- *startServers.sh*: starts servers on multiple machines specified by `hosts.txt`.
- *hosts.txt*: defines the machines, ports, and types of servers to start on each machine
   as `type::hostname::port` e.g. `[master | slave]::bb136-19.mines.edu::5555`.
- *testClientMethods.java*: Test cases and test runner for cluster operation.

## Design

 Our destributed forum is designed aroud a client/server java rmi model. The client will contact one of the 
servers, it can be any *slave* or even the *master/coordinator*, and send a request to it.  The servers will then
perform necessary interactions to reterive the most up to date articles or post an article with a unique id.
Most of the concistency and overall functionality of the system is hosted on the servers(fat server) with some caching on
the client side (thin client).

### Servers

  Our servers are split into two categories *Master/Coordinator* and *Slave*.  Our *master* node is determined at startup and is
not elected this simplifies our design but creates a single point of failure in our system. The *slave* servers register them
selves with the master node when they start this allows us to dynamically add and remove *slave* servers (How ever this was not
implemented) and doesn't require the *master* to know anything about the slaves until they start.
   <p>The *Master/Coordinator* server is the centeral hinge to our system and is responsible for the consistency of the system and ensuring
that all *Client* requests either (*1*) get the most uptodate versions of the articles or (*2*) post an article that has a unique id and will
eventually be seen by all servers. To accomplish this eventual consistency we implement three types of modles (*To be determined by the client*).
The first consistency model that the *master* uses is *Sequential consistency* model which ensures that any *client* reading from any of the
servers will see the articles in the same order as any other *client* reading from any of the other servers. The second consistency model that
the master uses is *Quorum consistency* which uses a set of the servers, *master* included, as a read quorum *Nr* and a write quorum *Nw* that follow
these constraints:
1. *Nr*+*Nw* > *N*
2. *Nw* > *N*/2
Quorum consistency ensures consistency by asking the read quorum(*Nr*) for articles when a read is requested or by writing to the 
write quorum(*Nw*) when a write is requested. The third type of consistency we use is *Read-your-Writes consistency* using local write protocol, 
this consistency model ensures that if a *client* writes to one server and then contacts another that it is guaranteed to read the article it just
posted.  This is accomplished through </p>


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

    ./runTests.sh

This will print out success if the tests pass other wise it will throw an error
This will also run the benchmark for the system then run `./getStats.rb <output csv filename>`
to get a csv file of the stats of all operations

