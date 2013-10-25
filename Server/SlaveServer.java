/**
* @Author Chris Card, Steven Rupert
* CSCI 565 project 1
* This file defines the Slave server methods
*/

package Server;

import Domain.Article;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * Slave node implementation. All BulletinBoard operations are proxied to the master.
 *
 * {@link #connectToMaster()} MUST be called in order to register this slave into a cluster.
 */
public class SlaveServer extends Node
{
    private static Logger log = LogManager.getLogger();
	public final String masterName, identifier;
	private Master master;

    /**
	* @param masterName string of the form of <masterhostname>:<mastersocket>
	* @param databaseName local database name to use.
	*/
	public SlaveServer(String masterName, String databaseName)
    {
        super(databaseName.replace(':', '_'));

        identifier = databaseName;
        this.masterName = masterName;
    }

    /**
     * Connects to the active master server so that a quorum can be formed
     */
	public void connectToMaster() throws Throwable
	{
		String hostport[] = masterName.split(":");
        int port  = Integer.parseInt(hostport[1]);
        Registry reg = LocateRegistry.getRegistry(hostport[0],port);
        master = (Master) reg.lookup("BulletinBoard");
        master.registerSlaveNode(this, identifier);
        log.info("Registered with master at {}", masterName);
	}

	public int post(Article article) throws RemoteException
	{
		return master.post(article);
	}

	public List<Article> getArticles() throws RemoteException
    {
		return master.getArticles();
	}

	public Article choose(int id) throws RemoteException
    {
        return master.choose(id);
    }
}