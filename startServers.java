/**
* @Author Chris Card, Steven Rupert
* This file reads in a host file and starts the servers based on that file
*//*


import java.lang.*;
import java.util.*;
import java.io.*;

class startServers 
{





    */
/**
     * This Method starts servers based on a host file that defines where to start the server
     * and the socket to start it on and whether or not it is a master or slave
     * @param file the name of the host file
     *//*

	public static void startServers(String file)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = "";
			String master = "";
			String path = getPath();

			while ((line = in.readLine()) != null) 
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
			in.close();
		} 
		catch(IOException e) 
		{
            System.err.println("Error in reading the host file:");
            e.printStackTrace();
            System.exit(2);
		}
	}

    */
/**
     * This stops the servers in the hosts file
     * @param file the host file
     *//*

    public static void stopServers(String file)
    {
        Set<String> hostnames = new HashSet<String>();
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = "";

            while ((line = in.readLine()) != null)
            {
                String params[] = line.split("::");
                hostnames.add(params[1]);
            }
            in.close();
        }
        catch(IOException e)
        {
            System.err.println("Error in reading the host file:");
            e.printStackTrace();
            System.exit(2);
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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


	public static void main(String[] args) 
	{
        if ((args.length == 0) || (args.length > 1))
        {
            System.out.println("Usage:\n"+
                                "java startServer [-start | -stop]");
        }
        else
        {
		    if (args[0].compareTo("-start") == 0) {startServers("hosts.txt");}
            else {stopServers("hosts.txt");}
        }
	}
}
*/
