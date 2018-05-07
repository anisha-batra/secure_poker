package poker.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PokerServer {
	
	static final int PORT = 1978;
	
	private static ArrayList<PokerTableThread> tables = new ArrayList();
	
	public static void main(String args[]) {
		
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            
            if (tables.isEmpty()) {
            		tables.add(new PokerTableThread());
            }
            
            PokerTableThread latestTable = tables.get(tables.size() - 1);
            if (!latestTable.hasCapacity()) {
            		tables.add(new PokerTableThread());
            		latestTable = tables.get(tables.size() - 1);
            }
            
            latestTable.addPlayer(socket, 100);
        }
    }
}
