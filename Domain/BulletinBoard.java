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
    /**
     * Posts the article to the cluster.
     * @param article article to post. The input {@link Article#id} is ignored.
     * @return the article's id on success.
     * @throws RemoteException if post failed.
     */
	int post(Article article, ConsistencyLevel level) throws RemoteException;

    /**
     * Retrieves up to 10 articles from the cluster with ids greater than `offset`.
     * @param offset id from which to start listing articles.
     * @throws RemoteException if listing articles fails.
     */
	List<Article> getArticles(int offset, ConsistencyLevel level) throws RemoteException;

    /**
     * Retrieves an article.
     * @param id the id of the desired article.
     * @throws RemoteException if the article is not found or other failure.
     */
	Article choose(int id, ConsistencyLevel level) throws RemoteException;
}
