/**
* @Author Chris Card, Steven Rupert
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
     *                  will be a string in the form of <masterhostname>:<mastersocket>
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

    /**
     * This method connects to the active master server so that a quorum can be formed
     */
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

    /**
     * This informs the master of the article to be posted
     * @param article the article to post
     * @throws RemoteException
     */
	public void post(Article article) throws RemoteException
	{
		master.post(article);
	}

    /**
     * This method gets all articles from the masters read quorum
     * @return
     * @throws RemoteException
     */
	public List<Article> getArticles() throws RemoteException
    {
		return master.getArticles();
	}

    /**
     * Asks the masters read quorum for the article with the specified id
     * @param id of the desired article
     * @return the desired article
     * @throws RemoteException  if the article is not found
     */
	public Article choose(int id) throws RemoteException
    {
        return master.choose(id);
    }

	//##################################################################
	// Server RPC Methods
	//##################################################################

    /**
     * This method writes the article to the article store when called
     * @param article to store
     */
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

    /**
     * This method gets this servers article with the specified id
     * @param id the id of the desired article
     * @return the desired article
     * @throws RemoteException  if the article is not found
     */
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

    /**
     * returns the servers local list of articles
     * @return the list of local articles
     * @throws RemoteException if there are no articles
     */
    @Override
    public List<Article> getLocalArticles() throws RemoteException
    {
        return articleStore.getAll();
    }
}