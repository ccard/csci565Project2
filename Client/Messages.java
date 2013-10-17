/**
* @Author Chris Card
* 9/16/13
* This is the message class the defines the serializable messages
*/

package Client;

import Compute.Message;
import java.io.Serializable;
import java.math.BigDecimal;

public class Messages implements Message, Serializable
{
	private static final long serialVersionUID = 227L;


	private final String message;
	private final String type;

	/**
	* Constructor
	* @param msg message to store
	* @param type the type of action to take when sent to the server, either send or receive
	*/
	public Messages(String msg, String type)
	{
		message = msg;
		this.type = type;
	}
	
	public String getMessage()
	{
		return message;
	}

	public String getMsgType()
	{
		return type;
	}


}
