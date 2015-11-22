package rpn.gateway.model.connection;

import io.netty.channel.Channel;
import java.util.ArrayList;

public class ConnectionHandler {

    private static final ConnectionHandler instance = new ConnectionHandler(2);

    public static ConnectionHandler getInstance() {
        return instance;
    }

    private int limit;

    private int noOfClients = 0;

    private Connection[] clients;

    private ArrayList<Connection> servers = new ArrayList<>();

    public ConnectionHandler(int limit) {
        this.limit = limit;
        this.clients = new Connection[limit];
    }

    public int getLimit() {
        return limit;
    }

    public int getNoOfClients() {
        return noOfClients;
    }

    public int getNoOfServers() {
        return servers.size();
    }

    private synchronized int getAvailableSlot() {
        int slot = -1;

        for (int i = 0; i < limit; i++) {
            if (clients[i] == null) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    public boolean clientExists(int id) {
        return id > 0 && id < clients.length && clients[id] != null;
    }

    public Connection getClient(int id) throws IndexOutOfBoundsException {
        if (id < 0 || id >= clients.length) {
            throw new IndexOutOfBoundsException();
        }

        return clients[id];
    }

    public Connection getServer(int index) throws IndexOutOfBoundsException {
        return servers.get(index);
    }

    public Connection register(Channel channel, String host) throws IndexOutOfBoundsException {
        return register(channel, host, -1);
    }

    public Connection register(Channel channel, String host, int port) throws IndexOutOfBoundsException {
        if (port < 0) {
            return registerClient(channel, host);
        } else {
            return registerServer(channel, host, port);
        }
    }

    private synchronized Connection registerClient(Channel channel, String host) throws IndexOutOfBoundsException {
        if (getNoOfClients() == getLimit()) {
            throw new IndexOutOfBoundsException();
        }

        int id = getAvailableSlot();

        Connection client = new Connection(channel);

        client.setAttribute("id", id);
        client.setAttribute("host", host);
        client.setAttribute("type", "CLIENT");

        noOfClients++;
        clients[id] = client;

        return client;
    }

    private synchronized Connection registerServer(Channel channel, String host, int port) {
        Connection server = new Connection(channel);

        server.setAttribute("host", host);
        server.setAttribute("port", port);
        server.setAttribute("type", "SERVER");
        server.setAttribute("isPrimary", getNoOfServers() == 0);

        servers.add(server);

        return server;
    }

    public void deregisterAll() {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) {
                deregister(clients[i]);
            }
        }
    }

    public void deregister(Connection connection) throws IllegalStateException {
        if (connection.getAttribute("type").equals("CLIENT")) {
            deregisterClient(connection);
        } else {
            deregisterServer(connection);
        }
    }

    private synchronized void deregisterClient(Connection connection) throws IllegalStateException {
        int id = (Integer) connection.getAttribute("id");

        connection.destruct();
        clients[id] = null;
        noOfClients--;
    }

    private synchronized void deregisterServer(Connection connection) throws IllegalStateException {
        connection.destruct();
        servers.remove(connection);
        if (connection.getAttribute("isPrimary").equals(Boolean.TRUE)) {
            electPrimary();
        }
    }

    private void electPrimary() throws IllegalStateException {
        if (servers.size() == 0)
            throw new IllegalStateException();
    }
}