/**
* @Author Chris Card
* 9/15/13
* This is the compute interface that is used by the server
*/

package Domain;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface BulletinBoard extends Remote {
	//Defines client rmi methods

    /**
     * This method posts the article to the write quorum
     * @param article article to post
     * @return the articles id
     * @throws RemoteException if post failed
     */
	int post(Article article) throws RemoteException;

    /**
     * This method gets all the articles from the read quorum
     * @return
     * @throws RemoteException if no articles are found
     */
	List<Article> getArticles() throws RemoteException;

    /**
     * This method returns a specific article
     * @param id the id of the desired article
     * @return the article
     * @throws RemoteException  if the article is not found
     */
	Article choose(int id) throws RemoteException;

	//Slave method
	void replicateWrite(Article article) throws RemoteException;

	//Master method
	/**
	* This method registars the Slave nodes with the master node
	* @param slave address of the slave node in the form of
	*		<slavehostname>:<portnumber>
    * @throws RemoteException if the slave node couldn't regesiter with the master
	*/
	void registerSlaveNode(BulletinBoard slave) throws RemoteException;

    // i.e. hit local database instead of quorum writes. Only nodes call these methods.
    // TODO split interface to make this more explicit
    Article getLocalArticle(int id) throws RemoteException;
    List<Article> getLocalArticles() throws RemoteException;
}
