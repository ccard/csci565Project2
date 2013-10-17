/**
* @Author Chris Card
* 9/16/13
* This Contains code for the clients
*/

package Client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import Compute.Compute;
import Compute.Message;
import java.io.*;

public class Client
{
	/**
	* This reades from the file passed to the client and creates a string representing that message
	* @param String filename to read from
	* @return String that represents the message in the text file
	*/
	public static String makeMessageText(String file)
	{
		//String builder reduces the number of concatinations needed to form a long string from
		//multiple strings
		StringBuilder message = new StringBuilder();

		if(file.isEmpty())
		{
			return "";
		}
		try{
			//read in the message from the file
			BufferedReader msg = new BufferedReader(new FileReader(new File(file)));
		
			String line = "";

			while((line = msg.readLine()) != null)
			{
				message.append(line);
				message.append("\n");
			}
		} catch (Exception e) {
			System.err.println("Could not read message file!");
			e.printStackTrace();
		}

		return message.toString();
	}
	
	/**
	* @param String list of arguments that must be in following order: 
	*        1 = hostname
	*			2 = socket
	*			3 = send | receive
	*			4 = message file if send
	*/
	public static void main(String args[])
	{
		String host="",sendReceive="",message="";
		int port = 5555;
		if(args.length >= 3)
		{
			host = args[0];
			try{
				port = Integer.parseInt(args[1]);
			} catch(Exception e){
				System.err.println("Invalide port");
				e.printStackTrace();
			}
			sendReceive = args[2];
			
			message = (args.length == 4 ? makeMessageText(args[3]) : "");
		}
		else{
			for(int i = 0; i < args.length; i++)
			{
				System.out.println(args[i]);
			}
			System.err.println("Invalide args");
		}
		try {
			String name = "Compute";

			Registry registry = LocateRegistry.getRegistry(host,port);
			Compute comp = (Compute) registry.lookup(name);

			Messages task = new Messages(message,sendReceive);
			Message ret = comp.sendReceive(task);

			//If the it is sending to server then checks to see if the returned message is null or
			//the same message
			if(sendReceive.compareTo("send") == 0)
			{
				if(null == ret)
				{
					System.out.println("Message was not received or it was lost!");
					return;
				}
				if(ret.getMessage().compareTo(task.getMessage()) == 0)
				{
					System.out.println("Message sent");
				}
			}
			else
			{
				if(null == ret)
				{
					System.out.println("The Server has no messages at this time!");
				}
				else
				{
					System.out.println(ret.getMessage());
				}
			}
		}catch(Exception e){
			System.err.println("Client exception:");
			e.printStackTrace();
		}
	}
}
