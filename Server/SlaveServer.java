/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Slave server methods
*/

package Server;

import Compute.Article;
import Compute.BulletinBoard;
import org.skife.jdbi.v2.DBI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SlaveServer implements BulletinBoard
{
    private final ArticleStore articleStore;

	public final String serverName,masterName;
	private BulletinBoard master;


    /**
	* @param masterName true if it is the master node false if it a slave node
	* @param serverName the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public SlaveServer(String masterName, String serverName)
	{
        // connect to embedded article database
        DBI dbi = new DBI("jdbc:h2:dbs/" + serverName.replace(':', '_'));
        articleStore = dbi.onDemand(ArticleStore.class);
        articleStore.initializeTable();

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

	public List<Article> getArticles() throws RemoteException
    {
		return master.getArticles();
	}

	public Article choose(int id) throws RemoteException
    {
        return master.choose(id);
    }

	//##################################################################
	// Server RPC Methods
	//##################################################################

	public void replicateWrite(Article article)
	{
        articleStore.insert(article);
	}

	/**
	* This method registers a slave node with the master node
	* @param slave slave bulletinBoard to register.
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

    @Override
    public Article getLocalArticle(int id) throws RemoteException
    {
        Article article = articleStore.get(id);
        if (null == article)
        {
            throw new RemoteException("404 not found");
        }
        return article;
    }

    @Override
    public List<Article> getLocalArticles() throws RemoteException
    {
        return articleStore.getAll();
    }
}