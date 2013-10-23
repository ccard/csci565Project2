Chris Card
Steven Ruppert
Project 2
--------------------


Files:

	Server/:


	Client/:


	lib/:

	Compute/:

	dbs/:


	runServer.java:

	runClient.java:

	startServers.java:

	
Design:



Running:
	
	1) Run command(in parent directory): make

	A) Servers:
			NOTE: Master server must be started before any slave server can be started
			1) To run a single server use command:
				java runServer -s <socket> [-master | -slave -mhost <master hostnem>:<master socket>]

				a) To start master (A master server must be running before a slave server can be started):
					java runServer -s <socket> -master

				b) To start a slave node (Master must be running before slave can be started):
					java runServer -s <socket> -slave -mhost <master hostame>:<master socket>

			2) To run a predefined set of servers run command:
				java startServers [-start | -stop]

				Note: this command reads the file called hosts.txt which must be in the following formate
				and the first line must be the master server definition:
					format:
						[master | slave]::<hostname>::<socket>
					ex:
						master::bb136-19.mines.edu::5555
						slave::bb136-10.mines.edu::5555
						slave::bb136-10.mines.edu::5556

	B) Clients:



	C) Tests:
