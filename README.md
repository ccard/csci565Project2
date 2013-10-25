Chris Card
Steven Ruppert
CSCI 565 Project 2: Distributed Bulletin Board
--------------------


## Files:

- Server/:

--*ArticleStore.java*: This is the local database for each server and is also used for some
concurrent access controls

--*Server.java*: This starts the specific types of servers either master or slave and binds
them to a port

--*MasterServer.java*: This contains the code specific to the *master/cordinator* node that controlls
the *read/write* quorums to ensure the eventual consistency of the system. The master can be contacted
directly by the client but the client shouldn't know if they are contacting the master or not.

--*SlaveServer.java*

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

To run an Client program use:

    ./runClient.sh COMMAND [REPLY_ID | OFFSET | ARTICLE_ID] [SERVER HOSTNAME] [SERVER PORT]

-***COMMAND***: Either POST, LIST, or GET all ***Case Insensitive***

-***POST***: Post a new article or reply to post. ***Content is read from STDIN***
       REPLY_ID ***MUST*** be present, if not replying to existing post set to 0.

-***LIST***: List articles on the server, there is a limit of *10* at a time
        OFFSET must be present and used to list *10* articles at a time
        set to *0* to start from begining.

-***GET***: Gets a specified article on the server. ARTICLE_ID must be present!

## Tests

-To run the tests:

    java testClientMethods

This will print out success if the tests pass other wise it will through an error

-To run the bench mark:

