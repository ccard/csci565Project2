package Server;

import Domain.Article;
import Domain.BulletinBoard;
import Domain.NotFound404Exception;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Slave node in the BulletinBoard cluster. Replicates writes and responds to
 * quorum read requests.
 */
public interface Slave extends BulletinBoard, Remote
{
    void replicateWrite(Article article) throws RemoteException;

    Article getLocalArticle(int id) throws RemoteException, NotFound404Exception;

    List<Article> getLocalArticles(int offset) throws RemoteException;

    /**
     * Sync articles with another node. After this method returns,
     * the caller's and callee's state should be in sync.
     * @param node node with which to sync.
     */
    void sync(Slave node) throws RemoteException;

    /**
     * Returns all articles known by this slave. Used in {@link #sync(Slave)} in order
     * to insert missing articles into the database.
     */
    List<Article> getAllArticles() throws RemoteException;
}
