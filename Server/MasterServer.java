/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Master server methods
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
import java.util.concurrent.atomic.AtomicInteger;

public class MasterServer implements BulletinBoard
{
	private List<BulletinBoard> slaves;
	private AtomicInteger masterArticleId;
	//Stores all messages that where recieved from clients
	private List<Article> articles;

	/**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public MasterServer()
	{
		super();
		masterArticleId = new AtomicInteger(0);
		slaves =  Collections.synchronizedList(new ArrayList<BulletinBoard>());

		articles =  Collections.synchronizedList(new ArrayList<Article>());
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

	/**
	* This method registars a slave node with the master node
	* @param is in the form of <hostname>:<port>
	*/
	public void registerSlaveNode(BulletinBoard slave) throws RemoteException
	{
		try
		{
			slaves.add(slave);

			System.out.println("success");
		} 
		catch(Exception e) 
		{
			System.err.println("Connot connect");
			e.printStackTrace();
		}
	}
}