package com.network;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Base64;

public class User {
    private String role;
    private String name;
    private String field;
    private String b64Image;
    private User currentExpert;
    private final PrintWriter writer;

    public User getCurrentContact() {
        return currentExpert;
    }

    public void setCurrentContact(User currentExpert) {
        this.currentExpert = currentExpert;
    }

    private int id;
    private static int lastId;

    public User(String name, String role, String field, String b64Image, PrintWriter writer) {
        this.role = role;
        this.name = name;
        this.b64Image = b64Image;
        this.field = field;
        lastId += 1;
        this.id = lastId;
        this.writer = writer;
    }

    public String getB64Image() {
        return b64Image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return b64Image;
    }

    public String getRole() {
        return role;
    }

    public String getField() {
        return field;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setB64Image(String b64Image) {
        this.b64Image = b64Image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static void setLastId(int lastId) {
        User.lastId = lastId;
    }

    private static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    public void receiveMessage(String msg) {
        writer.println(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!role.equals(user.role)) return false;
        if (!name.equals(user.name)) return false;
        if (!field.equals(user.field)) return false;
        if (!b64Image.equals(user.b64Image)) return false;
        if (!currentExpert.equals(user.currentExpert)) return false;
        return writer.equals(user.writer);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static User findFromId(int id) {
        System.out.println("finding user of id " + id);
        for (User u : Main.onlineUsers) {
            if (u.getId() == id) {
                return u;
            } else {
                System.out.println("no, it's " + u.getId());
            }
        }
        return null;
    }
}
