/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Master server methods
*/

package Server;

import Compute.Article;
import Compute.BulletinBoard;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.skife.jdbi.v2.DBI;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MasterServer implements BulletinBoard
{
    private final ExecutorService executorService;
    private final List<BulletinBoard> nodes;
    private List<BulletinBoard> slaves;
    private ArticleStore articleStore;

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

        executorService = Executors.newCachedThreadPool();
    }

	//##################################################################
	// Client RPC Methods
	//##################################################################

	public void post(Article input) throws RemoteException
    {
        final Article article = input.setId(articleStore.generateKey());

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

                        latch.countDown();
                    } catch (RemoteException e)
                    {
                        // TODO log
                        e.printStackTrace();
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

	public List<Article> getArticles() throws RemoteException
    {
        // strategy: collect all returned articles from the nodes into a set,
        // nodes will return all articles they know about, so we'll get the union
        // of all the known articles after all the nodes we wait for are done.
        // initialized to articles the master node (us) knows.
        final SortedSet<Article> articles =
                Collections.synchronizedSortedSet(Sets.<Article>newTreeSet());

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
                        articles.addAll(node.getLocalArticles());

                        latch.countDown();
                    } catch (RemoteException e)
                    {
                        // TODO log
                        e.printStackTrace();
                    }
                }
            });
        }

        try
        {
            boolean succeeded = latch.await(5, TimeUnit.SECONDS);
            if (succeeded)
            {
                return Lists.newArrayList(articles);
            } else
            {
                throw new RemoteException("Read timed out");
            }
        }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }

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
                        // TODO log
                        e.printStackTrace();
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


        // TODO broadcast get at quorum read level
    }

	//##################################################################
	// Server RPC Methods
	//##################################################################

	public void replicateWrite(Article article)
	{
        articleStore.insert(article);
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