all: server client test domain

server:
		  javac -cp lib/*:. Server/*.java

client:
		  javac -cp lib/*:. Client/Client.java

run:
		  javac runServer.java startServers.java

test:
		javac -cp lib/*:. testClientMethods.java

clean:
		  rm -f Server/*.class
		  rm -f Client/*.class
		  rm -f Domain/*.class
		  rm -f *.class
