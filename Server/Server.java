/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the rmi method sendReceive and also starts the server
*/

package Server;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import Compute.BulletinBoard;
import Compute.Article;
import java.util.concurrent.*;
import java.util.*;
import java.lang.Runtime;
import java.io.*;

public class Server
{

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
		try
		{
			String name = "Compute";

			int port  = socket(args);
			boolean master = isMaster(args);
			BulletinBoard engine = (master ? new MasterServer() : new SlaveServer(getMasterName(args),getHost()+":"+port));

			BulletinBoard stub = (BulletinBoard) UnicastRemoteObject.exportObject(engine,0);

			//This creates the rmiregistry so the user doesn't have to create it
			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind(name,stub);

			if (!master)
			{
				engine.registerSlaveNode(engine);
			}

			//Notifies user the server was bound to the socket
			System.out.println("Server bound to socket: "+port);
			System.out.println("EOF");
		} 
		catch (Exception e)
		{
			System.err.println("Server exception:");
			e.printStackTrace();
		}
	}
}
