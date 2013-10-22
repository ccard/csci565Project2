all: server client run

server:
		  javac -cp lib/*:. Server/*.java 

client:
		  javac Client/Client.java

run:
		  javac runClient.java runServer.java startServers.java

compute:
		  javac Compute/BulletinBoard.java Compute/Article.java
		  jar cvf Compute/compute.jar Compute/*.class 

clean:
		  rm -f Server/*.class
		  rm -f Client/*.class
		  rm -f Compute/*.class
		  rm -f *.class
