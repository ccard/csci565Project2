/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Master server methods
*/

package Server;

import Compute.Article;
import Compute.BulletinBoard;
import org.skife.jdbi.v2.DBI;

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
import com.google.common.util.concurrent.*;

public class MasterServer implements BulletinBoard
{
	private List<BulletinBoard> slaves;
    private ArticleStore articleStore;

	/**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public MasterServer()
	{
        slaves = Collections.synchronizedList(new ArrayList<BulletinBoard>());

        // connect to embedded article database
        DBI dbi = new DBI("jdbc:h2:mem:test");
        articleStore = dbi.onDemand(ArticleStore.class);
        articleStore.initializeTable();
	}

	//##################################################################
	// Client RPC Methods
	//##################################################################

	public void post(Article article)
	{
        article = article.setId(articleStore.insert(article));
        // TODO broadcast write at quorum write level
	}

	public List<Article> getArticles()
	{
		return articleStore.getAll();
        // TODO broadcast get at quorum read level
	}

	public Article choose(int id)
    {
        return articleStore.get(id);
        // TODO broadcast get at quorum read level
    }

	//##################################################################
	// Server RPC Methods
	//##################################################################

	public void replicateWrite(Article article)
	{
        // as the master, we don't need to do anything.
	}

	/**
	* This method registars a slave node with the master node
	* @param slave in the form of <hostname>:<port>
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
			System.err.println("Cannot connect");
			e.printStackTrace();
		}
	}
}