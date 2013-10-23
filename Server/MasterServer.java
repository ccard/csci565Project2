/**
* @Author Chris Card
* CSCI 565 project 1
* This file defines the Master server methods
*/

package Server;

import Compute.Article;
import Compute.BulletinBoard;
import com.google.common.collect.Iterables;
import org.skife.jdbi.v2.DBI;

import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import Compute.BulletinBoard;
import Compute.Article;
import java.util.concurrent.*;
import java.util.*;
import java.lang.Runtime;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.*;

public class MasterServer implements BulletinBoard
{
    private final ExecutorService executorService;
    private List<BulletinBoard> slaves;
    private ArticleStore articleStore;

    /**
	* @param master true if it is the master node false if it a slave node
	* @param the location of the master node if it is a slave node this will be
	* 		 a string in the form of <masterhostname>:<masterportnumber>
	*/
	public MasterServer()
	{
        slaves = Collections.synchronizedList(new ArrayList<BulletinBoard>());

        // connect to embedded article database
        DBI dbi = new DBI("jdbc:h2:mem:test");
        articleStore = dbi.onDemand(ArticleStore.class);
        articleStore.initializeTable();

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

        for (final BulletinBoard node : Iterables.concat(slaves, Collections.singletonList(this)))
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

	public List<Article> getArticles()
	{
		return articleStore.getAll();
        // TODO broadcast get at quorum read level
	}

	public Article choose(int id)
    {
        return articleStore.get(id);
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

			System.out.println("success");
		}
		catch(Exception e)
		{
			System.err.println("Cannot connect");
			e.printStackTrace();
		}
	}
}