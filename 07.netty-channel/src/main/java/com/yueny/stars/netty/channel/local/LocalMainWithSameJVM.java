package com.yueny.stars.netty.channel.local;

/**
 * 注意： 因为 LocalChannel 只能在同一 JVM 内通信；直接运行 LocalServer、LocalClient 时，没有在同 JVM 里先启动并绑定 LocalServer("my-local-server")，因此本地地址 local:my-local-server 未被绑定，出现 connection refused。
 *
 * 需要在此处运行，用于模拟同一进程内先起 LocalServer 再起 LocalClient。
 */
public class LocalMainWithSameJVM {
    public static void main(String[] args) throws Exception {
        final String serverName = "my-local-server";

        Thread serverThread = new Thread(() -> {
            try {
                new LocalServer(serverName).start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "local-server-thread");
        serverThread.setDaemon(true);
        serverThread.start();

        // 简单等待服务器完成绑定
        Thread.sleep(200);

        // 启动客户端连接并交互
        new LocalClient(serverName).start();

        // 客户端结束后退出进程（结束守护线程）
        System.exit(0);
    }
}
