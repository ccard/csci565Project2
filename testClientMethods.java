import Compute.Article;
import Compute.BulletinBoard;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Chris card, Steven Rupert
 * This mehtod runs test on the clients interaction with the server
 * because if the client fails then we know that the servers failed
 *
 * This is only tested with 3 servers and 2 clients
 */

public class testClientMethods
{
   ArrayList<String> servers;

   public testClientMethods()
   {
       servers = new ArrayList<String>();
       servers.add("master::bb136-19.mines.edu::5555");
       servers.add("slave::bb136-10.mines.edu::5555");
       servers.add("slave::bb136-10.mines.edu::5556");
       servers.add("slave::bb136-11.mines.edu::5555");


   }

   private void startServers()
   {

   }

    private class  Client
    {
        private ArrayList<BulletinBoard> servers;
        private ExecutorService executorService;
        /**
         * This is the constructor and it takes a list of strings
         * @param b List of strings of the servers to connect to
         */
        public Client(String... b)
        {
           servers = new ArrayList<BulletinBoard>();
           executorService = Executors.newCachedThreadPool();
           for (int i = 0; i < b.length; i++)
           {
               String line = b[i];
               String words[] = line.split("::");
               BulletinBoard temp = connectToServer(words[1],words[2]);
               assert temp != null;
               servers.add(temp);
           }
        }

        /**
         * This method connects to the specified server
         * @param host The hostname of server
         * @param socket The servers socket
         * @return server connection object or null if it couldn't connect
         */
        private BulletinBoard connectToServer(String host,String socket)
        {
            int port = 0;
            try
            {
                port = Integer.parseInt(socket);

                Registry reg = LocateRegistry.getRegistry(host,port);
                BulletinBoard comp = (BulletinBoard)reg.lookup("Compute");
                return comp;
            }
            catch (Exception e)
            {
                e.printStackTrace();

                assert false;
            }
            return null;
        }

        /**
         * Checks the number of servers it is connected to
         * @return true if it is connected to more than 1
         */
        public boolean connectedToMultiple()
        {
            return servers.size() > 1;
        }

        /**
         * This posts an article to the server if the client is connected
         * to multiple servers it chooses on at random
         * @param a The article to post
         * @throws AssertionError
         */
        public void postArticle(Article a) throws AssertionError
        {
            Random rand = new Random(System.currentTimeMillis());

            int choice = rand.nextInt(servers.size());

            try
            {
                servers.get(choice).post(a);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
                System.err.println("Fail in post");
                throw new AssertionError("Article potentially not posted");
            }
        }

        /**
         * This calls the servers choose method if connected to
         * multiple servers chooses one at random
         * @param i article id to choose
         * @return The article
         * @throws AssertionError
         */
        public Article chooseArticle(int i) throws AssertionError
        {
            Random rand = new Random(System.currentTimeMillis());

            int choice = rand.nextInt(servers.size());
            Article ret = null;
            try
            {
                ret = servers.get(choice).choose(i);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new AssertionError("Article not found");
            }
            return ret;
        }

        /**
         * This method returns a list of articles if the
         * Client is connected to multiple it chooses one at random
         * @return list of articles
         * @throws AssertionError
         */
        public List<Article> getArticles() throws AssertionError
        {
            Random rand = new Random(System.currentTimeMillis());

            int choice = rand.nextInt(servers.size());

            List<Article> ret = null;

            try
            {
               ret = servers.get(choice).getArticles();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new AssertionError("Articles not found");
            }

            return ret;
        }

        /**
         * This method simulates a client some how posting to multiple
         * servers at once
         * @param a  the list of articles to be posted
         * @throws AssertionError
         */
        public void postToAllConnected(Article... a) throws AssertionError
        {
            assert a.length == servers.size();

            final Article articles[] = a;
            int article = 0;
            final CountDownLatch latch = new CountDownLatch((slaves.size()));

            for (final BulletinBoard node : servers)
            {
                final int i = article;
                article++;
                executorService.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            node.post(articles[i]);
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
                    throw new AssertionError("Writes might not have happened");
                }
            } catch (InterruptedException ignored) {}
        }

    }


    public void main(String[] args)
    {

    }
}

