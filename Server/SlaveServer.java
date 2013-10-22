/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Slave server methods
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

public class SlaveServer implements BulletinBoard
{
	//Stores all messages that where recieved from clients
	private List<Article> articles;

	public final String serverName,masterName;
	private BulletinBoard master;
	
	
	/**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public SlaveServer(String masterName, String serverName)
	{
		super();
		
		articles = Collections.synchronizedList(new ArrayList<Article>());

		this.serverName = serverName;
		this.masterName = masterName;
		connectToMaster();
	}

	public void connectToMaster()
	{
		String name = "Compute";
		
		String hostport[] = masterName.split(":");
		try 
		{
			int port  = Integer.parseInt(hostport[1]);
			Registry reg = LocateRegistry.getRegistry(hostport[0],port);
			master = (BulletinBoard) reg.lookup(name);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(2);
		}
	}



	//##################################################################
	// Client RPC Methods
	//##################################################################

	public void post(Article article) throws RemoteException
	{
		master.post(article);
	}

	public List<Article> getArticles()
	{
		return null;
	}

	public Article choose(int id)
	{
		System.out.println("success");
		return null;
	}

	//##################################################################
	// Server RPC Methods
	//##################################################################

	public void replicateWrite(Article article)
	{

	}

	/**
	* This method registars a slave node with the master node
	* @param is in the form of <hostname>:<port>
	*/
	public void registerSlaveNode(BulletinBoard slave) throws RemoteException
	{
			try
			{
				master.registerSlaveNode(this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(2);
			}
	}
}