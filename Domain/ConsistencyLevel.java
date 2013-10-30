package Domain;

/**
 * Read/Write consistency levels possible. Clients of BulletinBoard specify
 * consistency per operation.
 */
public enum ConsistencyLevel
{
    /**
     *Writes will wait for all nodes in the cluster to respond before returning,
     * i.e if the write succeeds, ALL nodes have replicated the write.
     * Reads will represent latest known article(s) of ALL nodes.
     */
    ALL,
    /**
     * Writes will wait for n+2/1 nodes in the cluster to respond, i.e if the write succeeds,
     * a majority of nodes have replicated the write. Reads will represent latest
     * known article(s) in n/2 nodes in the cluster. If using QUORUM write consistency,
     * QUORUM read consistency must be used to guarantee that writes will be visible in the read.
     */
    QUORUM,
    /**
     * Writes will wait for one node in the cluster to respond. Reads will represent
     * the article(s) known to the first server to respond. If ONE write consistency is used,
     * then ALL read consistency must be used to ensure writes will be visible. ALL writes
     * can safely be read with ONE reads.
     */
    ONE
}
