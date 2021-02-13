package com.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

// One ClientSession object =====> one user
public class ClientSession implements Callable<Void> {
    private final Socket client;
    private User user;
    BufferedReader readClient;
    PrintWriter writeClient;

    public ClientSession(Socket client) {
        this.client = client;

        try {
            readClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writeClient= new PrintWriter(new OutputStreamWriter(client.getOutputStream()),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void call() throws Exception {
        System.out.println("trying to read from client");
        String clientInfo = readClient.readLine();
        System.out.println("message received, it's"+clientInfo);

        String[] clientInfoArr = clientInfo.split(";FayezIbrahimNivin;");
        System.out.println("split done");

        String name = clientInfoArr[0];
        String role = clientInfoArr[1];
        String field = clientInfoArr[2];
        String b64Image = clientInfoArr[3];

        System.out.println("about to define user");
        user = new User(name, role, field, b64Image, writeClient);
        System.out.println("defined user "+user.getId());

        for (User u:Main.onlineUsers) {
            u.receiveMessage("online");
            u.receiveMessage(String.join(";FayezIbrahimNivin;", ""+user.getId(), user.getName(), user.getRole(), user.getField(), user.getImage()));
        }

        Main.onlineUsers.add(user);

        user.receiveMessage(""+user.getId());

        for (User oldUser:Main.onlineUsers) {
            if (oldUser.getId() != user.getId()) {
                user.receiveMessage("online");
                user.receiveMessage(String.join(";FayezIbrahimNivin;", ""+oldUser.getId(), oldUser.getName(), oldUser.getRole(), oldUser.getField(), oldUser.getImage()));
            }
        }

        // continue receiving messages from the user of this thread
        // also sends messages to other users
        while (client.isConnected() || client.isClosed()) {
            System.out.println("waiting...");

            String s = readClient.readLine();

            System.out.println("received " + s + " from user" + user.getId());

            if (s != null) {
                String[] sInfo = s.split(";");

                if (sInfo[0].equals("message")) {
                    User from = user;
                    User to = User.findFromId(Integer.parseInt(sInfo[1]));
                    String message = sInfo[2];

                    for (var i:sInfo)
                        System.out.print(i+", ");
                    System.out.println();

                    System.out.println("found a message from user "+user.getName());
                    System.out.println("The message is "+message);

                    if (to != null) {
                        System.out.println("the message is to user "+to.getName());
                        to.receiveMessage("message;"+from.getId()+";"+message);
                        System.out.println("message send");
                    } else {
                        System.out.println("didn't find contact");
                    }
                }
            } else {
                System.out.println("client " + user.getId() + " disconnected");
                for (User u: Main.onlineUsers) {
                    if (u.getId() != user.getId())
                        u.receiveMessage(String.join(";FayezIbrahimNivin;", "offline", ""+user.getId() ));
                }
                break;
            }
        }

        System.out.println("removing "+user.getId());
        System.out.println(Main.onlineUsers.remove(user));

        return null;
    }


}


