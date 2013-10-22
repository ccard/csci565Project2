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
import java.util.*;

public class MasterServer implements BulletinBoard
{
	private List<BulletinBoard> slaves;
    private ArticleStore articleStore;

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
        articleStore.insert(article);
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