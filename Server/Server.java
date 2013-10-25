/**
* @Author Chris Card
* CSCI 565 project 2
* This file contains all the functionality to register each server on a port
*/

package Server;


import Domain.BulletinBoard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starts either a master or slave instance with command line args.
 */
public class Server
{
    private static Logger log = LogManager.getLogger();

    /**
     * This method checks to see if the input args wanted to start a master server
     * @param args from the command line
     * @return true if args contained "-master"
     */
	public static boolean isMaster(String[] args)
	{
        for (String arg : args)
        {
            if (arg.equals("-master"))
            {
                return true;
            }
        }
		return false;
	}

    /**
     * This gets the socket number from the command line args
     * @param args cmd line args
     * @return the socket number or -1 if none was found
     */
	public static int socket(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-s")) {
				try {
					return Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
		}

		return -1;
	}

    /**
     * This method resolves the masters hostname and socket from
     * the cmd line args
     * @param args cmd line arges
     * @return the masters hostname and socket in the form of
     *          <hostname>:<socket> or null
     */
	public static String getMasterName(String[] args)
	{
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-mhost")) {
				return args[i+1];
			}
		}
		return null;
	}

	/**
	* This gets the host name from the computer
	* @return the computers name
	*/
	public static String getHost()
	{
		String line, line2="";

		try{
			//Runs the command line call to hostname
			Process p = Runtime.getRuntime().exec("hostname");

			//reads the result from the command line call
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((line = b.readLine()) != null)
			{
				line2 = line.replace("\n","").replace("\r","");
			}

		} catch (Exception e) {
			System.err.println("Failed to find host name!");
			e.printStackTrace();
		}
		return line2;
	}

	/**
	* @param args list of arguments in the following format
	*     -s <socket> "Provides the socket number"
	*	  -master (only when instantiating the master server)
	* 	  -slave (only when instantiating the slave server)
	*	  -mhost <Master host name>:<socket> (only used if not the master node)
	*/
	public static void main(String[] args) throws Throwable
	{
        int port  = socket(args);
        boolean master = isMaster(args);
        String host = getHost();

        BulletinBoard engine =
                (master ? new MasterServer(host + "_" + port)
                        : new SlaveServer(getMasterName(args), host + ":" + port));

        BulletinBoard stub = (BulletinBoard) UnicastRemoteObject.exportObject(engine,0);

        //This creates the rmi registry so the user doesn't have to create it
        Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind("BulletinBoard",stub);

        if (!master)
        {
            ((SlaveServer) engine).connectToMaster();
            log.info("Slave started at {}:{}", host, port);
        } else {
            log.info("Master started at {}:{}", host, port);
        }

        System.out.println("EOF");
	}
}
