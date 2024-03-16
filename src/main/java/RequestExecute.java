import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class RequestExecute extends Thread{
    //将socket定义为成员变量
    private Socket socket;
    public RequestExecute(Socket socket){
        this.socket = socket;
    }

    public void run() {
        //从socket中取出输入流，然后取出数据
        InputStream in = null;
        InputStreamReader reader=null;
        BufferedReader bufferedReader=null;
        //声明输出流，指向客户端
        OutputStream out = null;
        PrintWriter pw = null;
        try {


            //从socket中获取字节输出流
            out = socket.getOutputStream();
            pw = new PrintWriter(out);
            //判断是否处于暂停状态
            if(Data.isPush){
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type: text/html;charset=utf-8");
                pw.println();
                pw.write("<h2>欢迎访问gys-WebServer</h2>");
                pw.write("<h2>服务器已关闭</h2>");
                pw.flush();
                return;
            }
            in = socket.getInputStream();
            reader = new InputStreamReader(in,"UTF-8");
            bufferedReader = new BufferedReader(reader);
            String line = null;
            int lineNum = 1;
            //存储请求路径
            String reqPath = "";
            String host = "";

            while ((line = bufferedReader.readLine())!=null){
                System.out.println(line);
                //解析请求行数据
                if(lineNum==1){
                    String[] infos = line.split(" ");
                    if(infos!=null||infos.length>2){
                        reqPath=infos[1];//请求路径
                    }else{throw new RuntimeException("请求行解析失败"+line);}
                }else{
                    //解析其他行，取出host的内容
                    String[] infos = line.split(": ");
                    if(infos!=null||infos.length==2){
                        //取出host
                        if(infos[0].equals("Host")){
                            host = infos[1];
                        }
                    }
                }
                lineNum ++;
                if(line.equals(""))break;
            }
            //输出请求信息
            if(!reqPath.equals("")){
            System.out.println("处理请求：http://"+host+reqPath);
            //根据请求相应客户端 /响应欢迎页面
                if(reqPath.equals("/")){
                    //没有资源的名称
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Type: text/html;charset=utf-8");
                    pw.println();
                    pw.write("<h2>欢迎访问gys-WebServer</h2>");
                    pw.flush();
                }else{
                    //查找对应的资源
                    //取出后缀
                    String ext = reqPath.substring(reqPath.lastIndexOf(".")+1);
                    reqPath = reqPath.substring(1);
                    if(reqPath.contains("/")){
                        //判断文件是否存在
                        File file = new File(Data.resourcePath+reqPath);
                        if(file.exists()&&file.isFile()){
                            response200(out, file.getAbsolutePath(),ext );
                        }else{
                            response404(out);
                        }
                    }else{
                        File root = new File(Data.resourcePath);
                        if(root.isDirectory()){
                            File[] list = root.listFiles();
                            boolean isExist = false;
                            for(File file:list){
                                if(file.isFile()&&file.getName().equals(reqPath)){
                                    //文件存在
                                    isExist = true;
                                    break;
                                }
                            }
                            if(isExist){
                                //文件存在
                                response200(out,Data.resourcePath+reqPath,ext);
                            }else{
                                //文件不存在
                                response404(out);
                            }
                        }else{
                            throw new RuntimeException("静态资源目录不存在"+Data.resourcePath);
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try{
                if(in!=null)in.close();
                if(reader!=null)reader.close();
                if(bufferedReader!=null)bufferedReader.close();
                if(pw!=null)pw.close();
                if(out!=null)out.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
    //将指定的文件输出到输出流中
    private void response200(OutputStream out,String filePath,String ext){
        PrintWriter pw = null;
        //准备输入流读取磁盘上的文件
        InputStream in = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {

            if(ext.equals("jpg")||ext.equals("png")||ext.equals("gif")){
                out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
                if(ext.equals("jpg"))
                out.write("Content-Type:image/jpg\r\n".getBytes(StandardCharsets.UTF_8));
                else if(ext.equals("png"))
                    out.write("Content-Type:image/png\r\n".getBytes(StandardCharsets.UTF_8));
                else if(ext.equals("gif"))
                    out.write("Content-Type:image/gif\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                in = new FileInputStream(filePath);
                int len = -1;
                byte[] buff = new byte[1024];
                while((len = in.read(buff))!=-1){
                    out.write(buff,0,len);
                    out.flush();
                }

            }else if(ext.equals("html")||ext.equals("js")||ext.equals("css")||ext.equals("json")){
                pw = new PrintWriter(out);
                pw.println("HTTP/1.1 200 OK");
                if(ext.equals("html"))
                    pw.println("Content-Type:text/html;charset=utf-8");
                else if(ext.equals("js"))
                    pw.println("Content-Type:application/x-javascript");
                else if(ext.equals("css"))
                    pw.println("Content-Type:text/css");
                else if(ext.equals("json"))
                    pw.println("Content-Type:application/json;charset=utf-8");
                pw.println();
                in = new FileInputStream(filePath);
                reader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(reader);
                String line = null;
                while((line = bufferedReader.readLine())!=null){
                    pw.println(line);
                    pw.flush();
                }

            }else{
                response404(out);
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(pw!=null)
                    pw.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void response404(OutputStream out){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(out);
            pw.println("HTTP/1.1 404");
            pw.println("Content-Type: text/html;charset=utf-8");
            pw.println();
            pw.write("<h2>欢迎访问gys-WebServer</h2>");
            pw.write("<h2>查找的资源不存在</h2>");
            pw.flush();
            System.out.println("响应欢迎页面");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(pw!=null)
                    pw.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
