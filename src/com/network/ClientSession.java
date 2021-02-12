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

        Main.onlineUsers.add(user);

        user.receiveMessage(""+user.getId());

        // continue receiving messages from the user of this thread
        // also sends messages to other users
        while (client.isConnected() || client.isClosed()) {
            System.out.println("waiting...");

            String s = readClient.readLine();

            System.out.println("received " + s + " from user" + user.getId());

            if (s != null) {
                String[] sInfo = s.split(";");

                if (sInfo[0].equals("change_field")) {
                    user.setField(sInfo[1]);
                } else if (sInfo[0].equals("connect")) {
                    System.out.println("found a connect request");
                    try {
                        User u = User.findFromId(Integer.parseInt(sInfo[1]));
                        System.out.println("connecting to user "+u.getId()+" from "+user.getId());
                        if (u != null) {
                            user.setCurrentContact(u);
                            writeClient.println("ok");
                        }                    else
                            writeClient.println("failed");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (sInfo[0].equals("end")) {
                    user.setCurrentContact(null);
                    writeClient.println("ok");
                } else if (sInfo[0].equals("message")) {
                    System.out.println("found a message from user "+user.getId());
                    User contact = user.getCurrentContact();

                    if (contact != null) {
                        System.out.println("the message is to user "+contact.getId());
                        contact.receiveMessage(sInfo[1]);
                        System.out.println("message send");
                    } else {
                        System.out.println("didn't find contact");
                    }
                } else if (sInfo[0].equals("GET_ONLINE_USERS")) {
                    System.out.println("A request to get online users");
                    String onlineUsersStr = "";
                    System.out.println("There's " + Main.onlineUsers.size() + " online users in main");
                    for (User u : Main.onlineUsers) {
                        // A user doesn't have to see himself as online
                        if (u.getId() != user.getId()) {
                            onlineUsersStr += String.join(";FayezIbrahimNivin;", "" + u.getId(), u.getName(), u.getRole(), u.getField(), u.getB64Image());
                            onlineUsersStr += ";user_seperator;";
                        }
                    }
                    user.receiveMessage(onlineUsersStr);
                }
            } else {
                System.out.println("client " + user.getId() + " disconnected");
                break;
            }
        }

        Main.onlineUsers.remove(user);

        return null;
    }


}


