package com.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static ArrayList<User> onlineUsers = new ArrayList<>();
    public static int availableThreads = 100;

    public static void main(String[] args) throws Exception {
        ServerSocket soc = new ServerSocket(6810);
        ExecutorService ex = Executors.newFixedThreadPool(100);
        System.out.println("server started");
        while (true) {
            if (availableThreads > 0) {
                Socket client = soc.accept();
                System.out.println("some user connected");
                ex.submit(new ClientSession(client));


                availableThreads--;
            }
        }
    }
}