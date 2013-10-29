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
     * Posts the article to the write quorum.
     * @param article article to post. The input {@link Article#id} is ignored.
     * @return the article's id on success.
     * @throws RemoteException if post failed.
     */
	int post(Article article, ConsistencyLevel level) throws RemoteException;

    /**
     * Retrieves all the articles from the read quorum.
     * @throws RemoteException if no articles are found.
     */
	List<Article> getArticles(ConsistencyLevel level) throws RemoteException;

    /**
     * Retrieves an article.
     * @param id the id of the desired article.
     * @throws RemoteException if the article is not found or other failure.
     */
	Article choose(int id, ConsistencyLevel level) throws RemoteException;
}
