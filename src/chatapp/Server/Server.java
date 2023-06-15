package chatapp.Server;

import chatapp.gui.CaptureView;
import chatapp.gui.ChatView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    
    private String serverName;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private List<Chat> clients;
    
    private OutputStream os;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader in;
    private PrintWriter out;
    
    private final boolean running = true;
    private final ChatView chatView;
    
    public Server(ChatView chatView) {
        this.chatView = chatView;
        clients= new ArrayList<>();
    }
    
    public String getServerName() {
        return serverName;
    }
    public void setServerName(String name) {
        serverName = name;
    }
    
    public void setConnection(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            while(running)
            { 
                clientSocket = serverSocket.accept();
                Chat receivingThread = new Chat(clientSocket);
                clients.add(receivingThread);
                receivingThread.start();
                System.out.println("Successfully connected"+clientSocket);
            }
           
        } catch (IOException ex) {
           System.err.println("ERROR: connection error");
           System.exit(0);
        }
    }
    public void listenData(ChatView chatView,Socket clientSocket) {
        try {
            is=clientSocket.getInputStream();
            isr=new InputStreamReader(is);
            in=new BufferedReader(isr);
            String receivedMessage = in.readLine();
            if (receivedMessage != null) {
                chatView.addRemoteMessage(receivedMessage);
            }
        } catch (IOException ex) {
            System.err.println("ERROR: error listening data");
        }
    }
     
    public void sendMessage(String msg) {
        for (Chat client : clients) {
            try {
                if(client.socket!=null)
                {
                    os=client.socket.getOutputStream();
                    out=new PrintWriter(os,true);
                    out.println(msg);
                }       
            } catch (IOException ex) {
                System.err.println("ERROR: error Sending data");
            }  
        }
       
    }
    public void closeConnection() {
        try {
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            System.err.println("ERROR: error closing connection");
        }
    }
    public static void main(String[] args) {
        ChatView chatView = new ChatView();
        Server server = new Server(chatView);
        
        CaptureView captureView = new CaptureView(chatView, true);
        captureView.setTitleText("Server login");
        captureView.setIpField("This computer");
        captureView.setPortField(1234);
        captureView.setIpEnable(false);
        captureView.setVisible(true);
        
        String serverName = captureView.getUsername();
        server.setServerName(serverName);
        
        int port = captureView.getPort();
        
        chatView.setServer(server);
        chatView.setUsername(serverName);
        chatView.setVisible(true); 
        
        server.setConnection(port);   
    }   
    class Chat extends Thread{
        private Socket socket;
        public Chat(Socket socket)
        {
            this.socket=socket;
        }

        @Override
        public void run() {
             while (running) {
                listenData(chatView,socket);
            }
        }    
    }
}
