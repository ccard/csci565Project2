package Server;

import Domain.BulletinBoard;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Master/coordinator node for the BulletinBoard cluster. Manages the global clock/ids and
 * quorum operations.
 */
public interface Master extends BulletinBoard, Remote
{
    /**
     * Registers the presence of a Slave in the cluster. Called by the slave.
     */
    public void registerSlaveNode(Slave slave, String identifier) throws RemoteException;
}
