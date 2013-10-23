import Compute.Article;
import Compute.BulletinBoard;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;

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



    private class  Client
    {
        ArrayList<BulletinBoard> servers;

        /**
         * This is the constructor and it takes a list of strings
         * @param b List of strings of the servers to connect to
         */
        public Client(String... b)
        {
           servers = new ArrayList<BulletinBoard>();

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


        public void postArticle(Article a)
        {
            Random rand = new Random(System.currentTimeMillis());

            int choice = rand.nextInt(servers.size());
        }

    }


    public void main(String[] args)
    {

    }
}

