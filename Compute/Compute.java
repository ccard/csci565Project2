/**
* @Author Chris Card
* 9/15/13
* This is the compute interface that is used by the server
*/

package Compute;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Compute extends Remote{
	//Defines the rmi methdo sendReceive
	Message  sendReceive(Message msg) throws RemoteException;
}
