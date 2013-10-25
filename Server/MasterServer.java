/**
* @Author Chris Card, Steven Rupert
* CSCI 565 project 1
* This file defines the Master server methods
*/

package Server;

import Compute.Article;
import Compute.BulletinBoard;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.skife.jdbi.v2.DBI;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MasterServer implements BulletinBoard
{
    private static Logger log = LogManager.getLogger();

    private final ExecutorService executorService;
    private final List<BulletinBoard> nodes;
    private List<BulletinBoard> slaves;
    private ArticleStore articleStore;

    /**
     * Constructor to Make masterserver node and initialize all necessary components
     * @param hostnameAndPort This contains the hostname and port of the master to
     *                        name the data base file after and should be in the form
     *                        of <hostname>:<socket>
     */
	public MasterServer(String hostnameAndPort)
	{
        slaves = Collections.synchronizedList(new ArrayList<BulletinBoard>());

        // nodes + this
        nodes = Collections.synchronizedList(new ArrayList<BulletinBoard>());
        // caution, leaks incomplete "this" reference, but I think we should be fine here
        nodes.add(this);

        // connect to embedded article database
        DBI dbi = new DBI("jdbc:h2:dbs/" + hostnameAndPort);
        articleStore = dbi.onDemand(ArticleStore.class);
        articleStore.initializeTable();
        articleStore.initializeCountTable();

        //Controlls conncurrent bord cast of replications to the quorum
        executorService = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("master-worker-%s").build());
    }

	//##################################################################
	// Client RPC Methods
	//##################################################################

    /**
     * This method gets the article id and then replicates the post to
     * all servers that are in the quorum
     * @param input article for master to post and then replicate to all
     *              other slaves
     * @throws RemoteException
     */
	public void post(Article input) throws RemoteException
    {
        //creates a new article with a unique id
        final int id;
        try
        {
            id = articleStore.generateKey();
        } catch (Exception e)
        {
            log.error("Couldn't write key", e);
            throw new RuntimeException("Couldn't generate key " + e.getMessage());
        }
        final Article article = input.setId(id);

        // TODO let client set either ALL quorum level or QUORUM level depending on their
        // replication needs
        final CountDownLatch latch = new CountDownLatch((slaves.size() + 1)/ 2 + 1); // write quorum

        for (final BulletinBoard node : nodes)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        node.replicateWrite(article);
                        log.info("Replicated write!");

                        latch.countDown();
                    } catch (RemoteException e)
                    {
                        log.error("Write not replicated!", e);
                    }
                }
            });
        }

        try
        {
            boolean succeeded = latch.await(5, TimeUnit.SECONDS);
            if (!succeeded)
            {
                throw new RemoteException("write probably didn't happen");
            }
        } catch (InterruptedException ignored) {}
    }

    /**
     * Gets all articles from the quorum of reads operations
     * @return list of all articles in the quorum with no duplicates
     * @throws RemoteException
     */
	public List<Article> getArticles() throws RemoteException
    {
        // strategy: collect all returned articles from the nodes into a set,
        // nodes will return all articles they know about, so we'll get the union
        // of all the known articles after all the nodes we wait for are done.
        // initialized to articles the master node (us) knows.
        final SortedSet<Article> articles =
                Collections.synchronizedSortedSet(Sets.<Article>newTreeSet());

        final CountDownLatch latch = new CountDownLatch(
                (int) Math.ceil((slaves.size() + 1.) / 2.)
        ); // read quorum

        // TODO randomize order to balance reads between slaves
        for (final BulletinBoard node : nodes)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        articles.addAll(node.getLocalArticles());

                        latch.countDown();
                    } catch (RemoteException e)
                    {
                        log.error("Couldn't get local articles!", e);
                    }
                }
            });
        }

        try
        {
            boolean succeeded = latch.await(5, TimeUnit.SECONDS);
            if (succeeded)
            {
                log.info("Listed {} articles", articles.size());
                return Lists.newArrayList(articles);
            } else
            {
                throw new RemoteException("Read timed out");
            }
        }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    /**
     * This method returns an article with the selected id from the read quorum
     * @param id of the article
     * @return the desired article
     * @throws RemoteException
     */
	public Article choose(final int id) throws RemoteException
    {
        // article will be the final returned article from the slaves
        // using an array so other threads can modify
        final Article[] article = new Article[1];

        final CountDownLatch latch = new CountDownLatch((slaves.size() + 1) / 2); // read quorum

        // TODO randomize order to balance reads between slaves
        for (final BulletinBoard node : nodes)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        article[0] = node.getLocalArticle(id);

                        latch.countDown();
                    } catch (RemoteException e) // thrown if not found
                    {
                        log.error("Couldn't get local article!", e);
                    }
                }
            });
        }

        try
        {
            boolean succeeded = latch.await(5, TimeUnit.SECONDS);
            if (succeeded)
            {
                if (article[0] == null)
                {
                    throw new RemoteException("404 not found");
                }
                else
                {
                    return article[0];
                }
            } else
            {
                throw new RemoteException("Read timed out");
            }
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

	//##################################################################
	// Server RPC Methods
	//##################################################################

    /**
     * This method writes the article that is to be replicated
     * @param article
     */
	public void replicateWrite(Article article)
	{
        log.debug("Replicating write...");
        try
        {
            articleStore.insert(article);
        } catch (Exception e)
        {
            log.error("Couldn't replicate write!", e);
            // XXX some exceptions are unserializable so when RMI tries to pass them to
            // the caller, we get NotSerializableExceptions instead of a proper message.
            // Thus, wrap message in a serializable runtimeException without a cause.
            throw new RuntimeException("Couldn't replicate write: " + e.getMessage());
        }
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
            nodes.add(slave);

			System.out.println("success");
		}
		catch(Exception e)
		{
			System.err.println("Cannot connect");
			e.printStackTrace();
		}
	}

    /**
     * This method returns the article from the local machine with the specified
     * id
     * @param id of the article to search for
     * @return desired article
     * @throws RemoteException throws error if article is not found
     */
    @Override
    public Article getLocalArticle(int id) throws RemoteException
    {
        Article article = null;
        try
        {
            article = articleStore.get(id);
        } catch (Exception e)
        {
            // XXX some exceptions are unserializable so when RMI tries to pass them to
            // the caller, we get NotSerializableExceptions instead of a proper message.
            // Thus, wrap message in a serializable runtimeException without a cause.
            throw new RuntimeException("Couldn't read article: " + e.getMessage());
        }
        if (null == article)
        {
            throw new RemoteException("404 not found");
        }
        return article;
    }

    /**
     * This method gets the list of local articles
     * @return the list of local articles
     * @throws RemoteException
     */
    @Override
    public List<Article> getLocalArticles() throws RemoteException
    {
        try
        {
            return articleStore.getAll();
        } catch (Exception e)
        {
            // XXX some exceptions are unserializable so when RMI tries to pass them to
            // the caller, we get NotSerializableExceptions instead of a proper message.
            // Thus, wrap message in a serializable runtimeException without a cause.
            throw new RuntimeException("Couldn't read articles: " + e.getMessage());
        }

    }
}