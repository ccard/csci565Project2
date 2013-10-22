/**
* This file reads in a host file and starts the servers based on that file
*/

import java.lang.*;
import java.util.*;
import java.io.*;

class startServers 
{

	public static void startServer(String path, String host, String args)
	{
		try{
		ProcessBuilder run = new ProcessBuilder("ssh",host,"cd "+path,"; java runServer "+args);
		run.redirectErrorStream(true);
		Process p = run.start();
		BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while ((line = b.readLine()) != null) {
			
		}
		} catch(Exception e) {
			System.err.println("Failed to start a server");
			e.printStackTrace();
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

	public static void startServers(String file)
	{
		try{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = "";
			String master = "";
			String path = getPath();

			while ((line = in.readLine()) != null) {
				String params[] = line.split("::");


				if (master.isEmpty() && params[0].compareTo("master") == 0) 
				{
					master = params[1]+":"+params
					startServer(path,params[1],)
				}
			}
		} catch(IOException e) {

		}
	}
}