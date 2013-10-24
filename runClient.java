/**
* @Author Chris Card, Steven Rupert
* This file is responsible for running the client
*/

import java.lang.Runtime;
import java.io.*;

public class runClient
{
	/**
	* This gets the host name from the computer
	* @return the computers name
	*/
	public static String getHost()
	{
		String line = "",line2="";

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
	* This method gets the path to the current directory
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
	* @param args the args must be in the following order
	*				1 = host of the server
	*				2 = socket of the server
	*				3 = [post | REPLY_ID | OFFSET | ARTICLE_ID]
	*/
	public static void main(String[] args)
	{
		String message,host,command,port;
		if (args.length == 4)
		{
			host = args[0];
			port = args[1];
			command = args[2];
			message = args[3];
		}
		else{
			System.out.println("Incorrect number of args!");
			System.out.println("java runClient <host> <port> <command> [REPLY_ID | OFFSET | ARTICLE_ID]");
			return;
		}
		try{
			String path = getPath();
			String thisComp = getHost();
			
			//builds and redirects the output of the command line call to the client program
			ProcessBuilder run = new ProcessBuilder("java","-Djava.rmi.server.codebase=http://"+thisComp+path+"/Client/","Client.Client",command,message,host,port);
			run.redirectErrorStream(true);
			Process p = run.start();

			p.waitFor();
			
			//Reads the output of the call to the command line program
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";
			
			while ((line = b.readLine()) != null)
			{
				System.out.println(line);
			}

		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
