package com.wqm.bio.samples;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 客服端
 *
 * @author Jacob Wu
 * @since 2021/1/8
 */
public class BIOClient {

    public static void main(String[] args) throws IOException {
        connect("127.0.0.1", BIOServer.PORT);
    }

    /**
     * 连接服务端
     *
     * @param hostname
     * @param port
     * @throws IOException
     */
    public static void connect(String hostname, int port) throws IOException {
        // 创建socket
        Socket socket = new Socket();

        socket.connect(new InetSocketAddress(hostname, port));

        System.out.println("已连接上服务端" + hostname + ":" + port + "，等待向服务端写数据。");

        // 输出流
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // 输入流
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String str;
        while ((str = br.readLine()) != null) {
            // 如果从键盘输入quit，则退出循环
            if ("quit".equals(str)) {
                break;
            }
            bw.write(str);
            // 需要写换行，因为server端每次读取一行
            bw.newLine();
            bw.flush();
        }

        // 关闭流
        br.close();

        // 关闭socket
        socket.close();
    }
}
