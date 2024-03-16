import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    //监听的端口
    private int port ;
    private MainFrame frame;
    public Server(int port,MainFrame frame){
        this.port=port;
        this.frame = frame;
    }
    @Override
    public void run() {
        //线程的主程序
        //声明Serversocket对象
        ServerSocket serverSocket = null;
        try{
            //创建Serversocket对象
            serverSocket = new ServerSocket(port);
            //开始监听
            System.out.println("开始监听...");
            frame.printLog("服务开始监听...");
            frame.printLog("监听端口："+port);
            frame.printLog("静态资源路径"+Data.resourcePath);
            while(Data.isRun) {//循环监听
                Socket socket = serverSocket.accept();
                System.out.println("接收到请求...");
                //将socket交给RequestExecute处理
                RequestExecute re = new RequestExecute(socket);
                re.start();
            }
            //关闭serversocket
            serverSocket.close();
            serverSocket = null;
            frame.printLog("服务监听停止");
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("端口"+port+"监听失败。"+e.getMessage());
        }
    }


}
