import Domain.Article;
import Domain.BulletinBoard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author Chris card, Steven Rupert
 * This mehtod runs test on the clients interaction with the server
 * because if the client fails then we know that the servers failed
 *
 * This is only tested with 3 servers and 2 clients
 */

public class testClientMethods
{
   ArrayList<String> serverstext;
   Client client1,client2,client3;

   public testClientMethods()
   {
       serverstext = new ArrayList<String>();
       serverstext.add("master::bb136-19.mines.edu::5555");
       serverstext.add("slave::bb136-12.mines.edu::5555");
       serverstext.add("slave::bb136-12.mines.edu::5556");
       serverstext.add("slave::bb136-13.mines.edu::5555");
       start();
       client1 = new Client(serverstext.get(2));
       client2 = new Client(serverstext.get(0),serverstext.get(3));
       client3 = new Client(serverstext.get(1));
   }

   private void start()
   {
        startServers(serverstext);
   }

    public void stop()
    {
        stopServers(serverstext);
    }

    /**
    * This method tests posting an artilce and choose a 1 of 3 clients to do it
    */
    public void testPostAndChoos()
    {
       Random rand = new Random(System.currentTimeMillis());

       switch (rand.nextInt(3))
       {
           case 0:
              int id = client1.postArticle(new Article("This is your friendly tester",0));
              Article verifypost = client2.chooseArticle(id);
              assert verifypost.id == id;
              break;
           case 1:
              id = client2.postArticle(new Article("This is your friendly tester",0));
              verifypost = client3.chooseArticle(id);
              assert verifypost.id == id;
              break;
           case 2:
              id = client1.postArticle(new Article("This is your friendly tester",0));
              verifypost = client2.chooseArticle(id);
              assert verifypost.id == id;
              break;
       }

       System.out.println("testPostAndChoose: pass");
    }

    /**
    * This Method tests listing all articles in the system
    */
    public void testListArticles()
    {
        Random rand = new Random(System.currentTimeMillis());
        List<Article> articles;
        switch(rand.nextInt(3))
        {
            case 0:
                articles = client1.getArticles();
                assert articles != null;
                assert articles.size() > 0;
                break;
            case 1:
                articles = client2.getArticles();
                assert articles != null;
                assert articles.size() > 0;
                break;
            case 3:
                articles = client3.getArticles();
                assert articles != null;
                assert articles.size() > 0;
                break;
        }

        for(Article a : articles)
        {
            System.out.println(a.content);
        }

        System.out.println("testListArticles: Passed");
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
                BulletinBoard comp = (BulletinBoard)reg.lookup("Domain");
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
        public int postArticle(Article a) throws AssertionError
        {
            Random rand = new Random(System.currentTimeMillis());

            int choice = rand.nextInt(servers.size());

            try
            {
                return servers.get(choice).post(a);
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
            final CountDownLatch latch = new CountDownLatch(servers.size());

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


    /**
     * Gets the path to the current directory
     * @return the path of the current directory
     */
    public static String getPath()
    {
        String line = "",line2="";

        try{
            Process p = Runtime.getRuntime().exec("pwd");

            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = b.readLine()) != null)
            {
                line2 = line.replace("\n","").replace("\r","");
            }
        } catch (Exception e) {
            System.err.println("Failed to find path!");
            e.printStackTrace();
        }
        return line2;
    }

    /**
     * This method starts a server either on a romote machine or the current machine
     * @param path the path to the base directory of the runServer file
     * @param host hostname of the computer to start the server
     * @param args the arguments to be passed to the server on the cmd line
     */
    public static void startServer(String path, String host, String args)
    {
        try
        {
            ProcessBuilder run = new ProcessBuilder("ssh",host,"cd "+path,"; ./runServer.sh "+args);
            run.redirectErrorStream(true);
            Process p = run.start();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = b.readLine()) != null)
            {
                if (line.compareTo("EOF") == 0) break;
                System.out.println(line);
            }
        }
        catch(Exception e)
        {
            System.err.println("Failed to start a server");
            e.printStackTrace();
        }
    }

    /**
     * This Method starts servers based on a host file that defines where to start the server
     * and the socket to start it on and whether or not it is a master or slave
     * @param file the name of the host file
     */
    public static void startServers(ArrayList<String> file)
    {
        String master = "";
        String path = getPath();

        for (String line : file)
        {
            String params[] = line.split("::");


            if (master.isEmpty() && params[0].compareTo("master") == 0)
            {
                master = params[1]+":"+params[2];
                startServer(path,params[1],"-s "+params[2]+" -master");
            }
            else if (master.isEmpty())
            {
                System.err.println("Host file incorrect formate master should be the first value");
                System.exit(2);
            }

            if (params[0].compareTo("slave") == 0)
            {
                startServer(path,params[1],"-s "+params[2]+" -slave -mhost "+master);
            }
        }
    }

    /**
     * This stops the servers in the hosts file
     * @param file the host file
     */
    public static void stopServers(ArrayList<String> file)
    {
        Set<String> hostnames = new HashSet<String>();

        for (String line : file)
        {
            String params[] = line.split("::");
            hostnames.add(params[1]);
        }

        for (String host : hostnames)
        {
            ProcessBuilder run = new ProcessBuilder("ssh",host,"pkill java");

            run.redirectErrorStream(true);
            try {
                Process p = run.start();

                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while ((line = in.readLine()) != null)
                {
                    System.out.println(line);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        testClientMethods t = new testClientMethods();
        t.testPostAndChoos();
        t.stop();
    }
}

