/**
* @Author Chris Card
* 9/15/13
* This is the compute interface that is used by the server
*/

package Compute;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public interface BulletinBoard extends Remote{
	//Defines client rmi methods
	void post(Article article) throws RemoteException;
	List<Article> getArticles() throws RemoteException;
	Article choose(int id) throws RemoteException;

	//Slave method
	void replicateWrite(Article article) throws RemoteException;

	//Master method
	/**
	* This method registars the Slave nodes with the master node
	* @param the address of the slave node in the form of
	*		<slavehostname>:<portnumber>
	*/
	void registerSlaveNode(BulletinBoard slave) throws RemoteException;

    // i.e. hit local database instead of quorum writes. Only nodes call these methods.
    // TODO split interface to make this more explicit
    Article getLocalArticle(int id) throws RemoteException;
    List<Article> getLocalArticles() throws RemoteException;
}
