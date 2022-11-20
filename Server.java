package application;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    static int Players = 0;
    static ServerSocket serverSocket;
    static ArrayList<BufferedReader> bufferedReaders = new ArrayList<>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<>();
    static ArrayList<String> message = new ArrayList<>();
    private static int[][] chessBoard = new int[3][3];

    private static int FinalCheck() {
        if (chessBoard[0][0]==chessBoard[1][1]&&chessBoard[1][1]==chessBoard[2][2]&&chessBoard[2][2]==1)
            return 1;
        if (chessBoard[0][2]==chessBoard[1][1]&&chessBoard[1][1]==chessBoard[2][0]&&chessBoard[2][0]==1)
            return 1;
        for (int i = 0; i < 3; i++) {
            if(chessBoard[0][i]==chessBoard[1][i]&&chessBoard[1][i]==chessBoard[2][i]&&chessBoard[2][i]==1)
                return 1;
            if(chessBoard[i][0]==chessBoard[i][1]&&chessBoard[i][1]==chessBoard[i][2]&&chessBoard[i][2]==1)
                return 1;
        }
        if (chessBoard[0][0]==chessBoard[1][1]&&chessBoard[1][1]==chessBoard[2][2]&&chessBoard[2][2]==2)
            return 2;
        if (chessBoard[0][2]==chessBoard[1][1]&&chessBoard[1][1]==chessBoard[2][0]&&chessBoard[2][0]==2)
            return 2;
        for (int i = 0; i < 3; i++) {
            if(chessBoard[0][i]==chessBoard[1][i]&&chessBoard[1][i]==chessBoard[2][i]&&chessBoard[2][i]==2)
                return 2;
            if(chessBoard[i][0]==chessBoard[i][1]&&chessBoard[i][1]==chessBoard[i][2]&&chessBoard[i][2]==2)
                return 2;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (chessBoard[i][j]==0)
                    return 0;
            }
        }
        return 3;
    }

    public static void main(String[] args) {
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
                    if (Players == 2) continue;
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        bufferedReaders.add(bufferedReader);
                        new Receive(socket, bufferedReader).start();
                        printWriters.add(new PrintWriter(socket.getOutputStream()));
                        printWriters.get(Players).println("SetID,"+Players);
                        printWriters.get(Players).flush();
                        System.out.println("Number of Players: " + ++Players);
                        if (Players == 2) {
                            for (int i = 0; i < printWriters.size(); i++) {
                                printWriters.get(i).println("StartGame,0");
                                printWriters.get(i).flush();
                            }
                        }
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
                    else if (opcode[0].equals("Gaming")){
                        int player = Integer.parseInt(opcode[1]);
                        int i = Integer.parseInt(opcode[2]);
                        int j = Integer.parseInt(opcode[3]);
                        // change the state of server
                        chessBoard[i][j] = player + 1;
                        // change the state of each client
                        message.add(readLine);
                        // check if game set
                        int res=FinalCheck();
                        if (res!=0) {
                            message.add("FinalCheck,"+res);
                            chessBoard=new int[3][3];
                        }
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
                        for (int i = 0; i < printWriters.size(); i++) {
                            printWriters.get(i).println(string);
                            printWriters.get(i).flush();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
