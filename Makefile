all: server client run

server:
		  javac  Server/Server.java

client:
		  javac Client/Client.java Client/Messages.java

run:
		  javac runClient.java runServer.java

compute:
		  javac Compute/Compute.java Compute/Message.java
		  jar cvf Compute/compute.jar Compute/*.class 

clean:
		  rm -f Server/*.class
		  rm -f Client/*.class
		  rm -f Compute/*.class
		  rm -f *.class
