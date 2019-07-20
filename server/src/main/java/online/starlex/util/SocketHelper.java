package online.starlex.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class SocketHelper {
    Map<String, java.net.Socket> socketPool = new HashMap<>(2);
    private ServerSocket serverSocket;

    private int port = 8080;

    void open() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    void join() throws IOException {
        //界面输出 正在等待玩家1接入
        System.out.println("INFO:Waiting for Player1...");
        socketPool.put("Player1", serverSocket.accept());
        //界面输出 正在等待玩家2接入
        System.out.println("INFO:Waiting for Player2...");
        socketPool.put("Player2", serverSocket.accept());
        System.out.println("INFO:Complete.");
    }

    public void setPort(int port) {
        this.port = port;
    }

}
