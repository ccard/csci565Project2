/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the rmi method sendReceive and also starts the server
*/

package Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import Compute.BulletinBoard;
import Compute.Article;
import java.util.concurrent.*;
import java.util.*;
import java.lang.Runtime;
import java.io.*;

public class Server implements BulletinBoard
{
	//Stores all messages that where recieved from clients
	private ConcurrentLinkedQueue<Article> articles;

	private boolean isMaster;
	private String serverName,masterName;
	private BulletinBoard master;
	private ArrayList<BulletinBoard> slaves;
	private int masterArticleId;
	
	/**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public Server(boolean master, String masterName, String serverName)
	{
		super();
		if (master) {
			masterArticleId = 0;
			slaves = new ArrayList<BulletinBoard>();
		}
		articles = new ConcurrentLinkedQueue<Article>();

		isMaster = master;
		this.serverName = serverName;
		this.masterName = masterName;
		connectToMaster();
	}

	public void connectToMaster()
	{
		if (!isMaster) {
			String name = "Compute";
			System.out.println(masterName);
			String hostport[] = masterName.split(":");
			try {
				int port  = Integer.parseInt(hostport[1]);
				Registry reg = LocateRegistry.getRegistry(hostport[0],port);
				master = (BulletinBoard) reg.lookup(name);

			} catch (Exception e){
				e.printStackTrace();
				System.exit(2);
			}
		}
	}



	//##################################################################
	// Client RPC Methods
	//##################################################################

	public void post(Article article)
	{

	}

	public List<Article> getArticles()
	{
		return null;
	}

	public Article choose(int id)
	{
		return null;
	}

	//##################################################################
	// Server RPC Methods
	//##################################################################

	public void replicateWrite(Article article)
	{

	}

	public void registerSlaveNode(String slaveNodeAddress)
	{

	}
	
	//###################################################################
	// Input parsing methods
	//###################################################################

	public static boolean isMaster(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareTo("-master") == 0) {
				return true;
			}
		}
		return false;
	}

	public static int socket(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareTo("-s") == 0) {
				try {
					return Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
		}

		return -1;
	}

	public static String getMasterName(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			if (args[i].compareTo("-mhost") == 0) {
				return args[i+1];
			}
		}
		return null;
	}

	/**
	* This gets the host name from the computer
	* @return the computers name
	*/
	public static String getHost()
	{
		String line = "",line2="";

		try{
			//Runs the command line call to hostname
			Process p = Runtime.getRuntime().exec("hostname");

			//reads the result from the command line call
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = b.readLine()) != null)
			{
				line2 = line.replace("\n","").replace("\r","");
			}

		} catch (Exception e) {
			System.err.println("Failed to find host name!");
			e.printStackTrace();
		}
		return line2;
	}

	/**
	* @param list of arguments in the following formate
	*     -s <socket> "Provides the socket number"
	*	  -master (only when instantiating the master server)
	* 	  -slave (only when instantiating the slave server)
	*	  -mhost <Master host name>:<socket> (only used if not the master node)
	*/
	public static void main(String[] args)
	{
		try{
			String name = "Compute";

			int port  = socket(args);
			BulletinBoard engine = new Server(isMaster(args),getMasterName(args),getHost()+":"+port);

			BulletinBoard stub = (BulletinBoard) UnicastRemoteObject.exportObject(engine,0);

			//This creates the rmiregistry so the user doesn't have to create it
			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind(name,stub);

			//Notifies user the server was bound to the socket
			System.out.println("Server bound to socket: "+port);
			System.out.println("EOF");
		} catch (Exception e){
			System.err.println("Server exception:");
			e.printStackTrace();
		}
	}
}
