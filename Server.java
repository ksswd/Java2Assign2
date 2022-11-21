package application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static int Players = 0;
    static ServerSocket serverSocket;
    static ArrayList<BufferedReader> bufferedReaders = new ArrayList<>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<>();
    static ArrayList<String> message = new ArrayList<>();

    public static void main(String[] args) {
//        ExecutorService service = Executors.newFixedThreadPool(100);
        try {
            serverSocket = new ServerSocket(12312);
            new AcceptSocketThread().start();
            new Send().start();
            System.out.println("Number of Players: " + Players);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class AcceptSocketThread extends Thread {
        public void run() {
            while (this.isAlive()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        bufferedReaders.add(bufferedReader);
                        new Receive(socket, bufferedReader).start();
                        printWriters.add(new PrintWriter(socket.getOutputStream()));
                        printWriters.get(Players).printf("SetID,%d%n", Players % 2);
                        printWriters.get(Players).flush();
                        System.out.println("Number of Players: " + ++Players);
                        if (Players % 2 != 0 || Players == 0) {
                            continue;
                        }
                        printWriters.get(Players-1).println("StartGame,0");
                        printWriters.get(Players-2).println("StartGame,0");
                        printWriters.get(Players-1).flush();
                        printWriters.get(Players-2).flush();
//                            for (int i = Players; i < printWriters.size(); i+=2) {
//                                printWriters.get(i).println("StartGame,0");
//                                printWriters.get(i+1).println("StartGame,0");
//                                printWriters.get(i).flush();
//                                printWriters.get(i+1).flush();
//                            }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Receive extends Thread {
        Socket socket;
        BufferedReader bufferedReader;

        public Receive(Socket socket, BufferedReader bufferedReader) {
            this.socket = socket;
            this.bufferedReader = bufferedReader;
        }

        public void run() {
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) continue;
                    String[] opcode = readLine.split(",");
                    if (opcode[0].equals("Disconnect")) {

                        System.out.println("Number of Players: " + --Players);
                        if (Players == 0) {
                            printWriters.clear();
                            bufferedReaders.clear();
                        }
                        else {
                            message.add("Disconnect");
                        }
                        socket.close();
                        bufferedReader.close();
                        return;
                    }
                    else if (opcode[0].equals("Gaming")||opcode[0].equals("FinalCheck")){
                        message.add(readLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Send extends Thread {
        public void run() {
            while (this.isAlive()) {
                try {
                    if (!message.isEmpty()) {
                        String string = message.remove(0);
//                        System.out.println(string);
                        for (int i = 0; i < printWriters.size(); i+=2) {
                            printWriters.get(i).println(string);
                            printWriters.get(i).flush();
                            printWriters.get(i+1).println(string);
                            printWriters.get(i+1).flush();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
