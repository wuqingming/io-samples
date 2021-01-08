package com.wqm.bio.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * server启动器
 *
 * @author Jacob Wu
 * @since 2021/1/8
 */
public class BIOThreadPoolServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        startup();
    }

    /**
     * 启动
     *
     * @throws IOException
     */
    public static void startup() throws IOException {
        // 创建服务端socket
        ServerSocket serverSocket = new ServerSocket();

        // 绑定端口
        serverSocket.bind(new InetSocketAddress(PORT));
        System.out.println("服务器已启动，等待连接...  127.0.0.1:" + PORT);

        Executor executor = Executors.newFixedThreadPool(10);
        // 循环接收客户端连接
        while (true) {
            // 获取客户端socket，等待客户端连接
            Socket clientSocket = serverSocket.accept();
            executor.execute(new ClientHandler(clientSocket));
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
            InetSocketAddress clientAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
            String clientIp = clientAddress.getHostName();
            System.out.println("接收到客户端连接" + clientIp);
            BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String str;
            while ((str = br.readLine()) != null) {
                if ("quit".equals(str)) {
                    break;
                }
                System.err.println(str);
            }
            // 关闭流
            br.close();
            // 关闭客户端socket
            clientSocket.close();
            System.out.println("客户端连接结束" + clientIp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
