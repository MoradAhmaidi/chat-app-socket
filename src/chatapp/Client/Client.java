package chatapp.Client;

import chatapp.gui.CaptureView;
import chatapp.gui.ChatView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private String clientName;
    private Socket clientSocket;
    
    private OutputStream os;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader in;
    private PrintWriter out;
    
    private final boolean running = true;
    private final ChatView chatView;
    
    public Client(ChatView chatView) {
        this.chatView = chatView;
    }
    
    public String getClientName() {
        return clientName;
    }
    public void setClientName(String name) {
        clientName = name;
    }
    public void setConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            sendMessage("is Connected");
            Thread receivingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        listenData(chatView);
                    }
                }
            });
            receivingThread.start();
            System.out.println("Successfully connected to " + ip + ":" + port);
        } catch (IOException ex) {
            System.err.println("ERROR: connection error");
            System.exit(0);
        }
    }
    public void listenData(ChatView chatView) {
        try {
            is=clientSocket.getInputStream();
            isr=new InputStreamReader(is);
            in=new BufferedReader(isr);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String receivedMessage = in.readLine();
            if (receivedMessage != null) {
                chatView.addRemoteMessage(receivedMessage);
            }
        } catch (IOException ex) {
            System.err.println("ERROR: error listening data");
        }
    }
    public void sendMessage(String msg) {
        try {
            os=clientSocket.getOutputStream();
            out=new PrintWriter(os,true);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(clientName+" : "+msg);
        } catch (IOException ex) {
             System.err.println("ERROR: error sending data");
        }
    }
    public void closeConnection() {
        try {
            sendMessage("is Desconnected");
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println("ERROR: error closing connection");
        }
    } 
    
    public static void main(String [] args) {
        ChatView chatView = new ChatView();
        Client cli = new Client(chatView);
        
        CaptureView captureView = new CaptureView(chatView, true);
        captureView.setTitleText("Client login");
        captureView.setIpEnable(true);
        captureView.setVisible(true);
        
        String cliName = captureView.getUsername();
        cli.setClientName(cliName);
        
        String ipMachine = captureView.getIP();
        int portMachine = captureView.getPort();
        cli.setConnection(ipMachine, portMachine);
        
        chatView.setClient(cli);
        chatView.setUsername(cliName);
        chatView.setVisible(true);
        
    }
}
