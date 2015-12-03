package rpn.server.model.connection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class ReplicaHandler {

    private static ReplicaHandler instance = new ReplicaHandler();

    public static ReplicaHandler getInstance() {
        return instance;
    }

    private HashMap<Integer, Replica> replicas = new HashMap<Integer, Replica>();

    public void registerReplica(Replica connection) {
        synchronized (this) {
            replicas.put(connection.hashCode(), connection);
        }
    }

    public void deregisterReplica(Replica connection) {
        synchronized (this) {
            replicas.remove(connection.hashCode());
        }
    }

    private Collection<Replica> getReplicas() {
        Collection<Replica> replicas;
        synchronized (this) {
            replicas = Collections.unmodifiableCollection(this.replicas.values());
        }
        return replicas;
    }
}
