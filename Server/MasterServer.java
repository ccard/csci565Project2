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

public class MasterServer implements BulletinBoard
{
	private ArrayList<BulletinBoard> slaves;
	private int masterArticleId;
	//Stores all messages that where recieved from clients
	private ConcurrentLinkedQueue<Article> articles;

	/**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public MasterServer()
	{
		super();
		masterArticleId = 0;
		slaves = new ArrayList<BulletinBoard>();

		articles = new ConcurrentLinkedQueue<Article>();
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