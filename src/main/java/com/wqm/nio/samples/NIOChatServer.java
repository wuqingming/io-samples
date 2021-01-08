package com.wqm.nio.samples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * nio聊天服务
 *
 * @author Jacob Wu
 * @since 2021/1/8
 */
public class NIOChatServer {
    private static final Charset CHARSET = Charset.forName("UTF-8");
    public static final int PORT = 8080;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector = null;

    public static void main(String[] args) throws IOException {
        new NIOChatServer().startup();
    }

    /**
     * 启动
     */
    private void startup() throws IOException {
        // 创建server channel
        serverSocketChannel = ServerSocketChannel.open();
        // 配置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 监听端口
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        // 创建一个选择器
        selector = Selector.open();
        System.out.println("聊天服务已启动...127.0.0.1:" + PORT);

        service();
    }

    /**
     * 负责监控上线、下线以及转发消息
     */
    private void service() throws IOException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            // 获得Selector的selected-keys集合
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();

            //从集合中依次取出SelectionKey对象,判断是那种事件发生，然后进行处理
            while (it.hasNext()) {
                SelectionKey key = null;
                try {
                    // 处理selectionKey 取出第一个selectionKey
                    key = (SelectionKey) it.next();
                    // 把selectionKey从selected-key集合中删除
                    it.remove();

                    // 这个key 标识连接就绪事件 处理
                    if (key.isAcceptable()) {
                        //获得与SelectionKey相连的ServerSocketChannel
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        //获得与客户端连接的SocketChannel
                        SocketChannel sockChannel = ssc.accept();
                        System.out.println("接收到客户端连接，来自：" + sockChannel.socket().getInetAddress() + ":"
                                + sockChannel.socket().getPort());

                        //设置SocketChannel为非阻塞模式
                        sockChannel.configureBlocking(false);
                        //创建一个用于存放用户发送来的数据的缓冲区
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //SocketChannel向Selector注册读就绪事件和写就绪事件   关联了一个buffer
                        //这个byteBuffer将作为附件与新建的selectionKey对象关联
                        sockChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);

                    }

                    // 读事件已就绪
                    if (key.isReadable()) {
                        receive(key);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        //使得这个selctionkey失效
                        //使得selectory不再监控这个selectionkey感兴趣的事件
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch (Exception e2) {
                        e.printStackTrace();
                    }

                }

            }


        }

    }

    /**
     * 处理读就绪事件
     * 把收到的数据放入buffer
     *
     * @param key
     * @throws IOException
     */
    public void receive(SelectionKey key) throws IOException {
        //获得与SelectionKey关联的Sockethannel
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //创建一个byteBuffer,用于存放读到的数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        /**
         * 循环读取客户端请求信息
         */
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             * 切换buffer为读模式
             */
            byteBuffer.flip();

            /**
             * 读取buffer中的内容
             */
            request += decode(byteBuffer);
        }

        /**
         * 将客户端发送的请求信息 广播给其他客户端
         */
        if (request.length() > 0) {
            // 广播给其他客户端
            broadCast(socketChannel, request);
        }

    }

    /**
     * 广播给其他客户端
     */
    private void broadCast(SocketChannel sourceChannel, final String request) {
        /**
         * 获取到所有已接入的客户端channel
         */
        Set<SelectionKey> selectionKeySet = selector.keys();

        /**
         * 循环向所有channel广播信息
         */
        for (SelectionKey selectionKey : selectionKeySet) {
            SelectableChannel targetChannel = selectionKey.channel();

            // 剔除发消息的客户端
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    // 将信息发送到targetChannel客户端
                    ((SocketChannel) targetChannel).write(encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 编码  把字符串转换成自己序列
     *
     * @param string
     * @return
     */
    private ByteBuffer encode(String string) {
        return CHARSET.encode(string);
    }

    /**
     * 解码  把字节序列转换为字符串
     *
     * @param buffer
     * @return
     */
    private String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = CHARSET.decode(buffer);
        return charBuffer.toString();
    }
}
