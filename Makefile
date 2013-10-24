all: server client test

server:
		  javac -cp lib/*:. Server/*.java

client:
		  javac -cp lib/*:. Client/Client.java

run:
		  javac runServer.java startServers.java

test:
		javac -cp lib/*:. testClientMethods.java

compute:
		  javac Compute/BulletinBoard.java Compute/Article.java
		  jar cvf Compute/compute.jar Compute/*.class

clean:
		  rm -f Server/*.class
		  rm -f Client/*.class
		  rm -f Compute/*.class
		  rm -f *.class
