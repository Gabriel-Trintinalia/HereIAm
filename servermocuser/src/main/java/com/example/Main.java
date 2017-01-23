package com.example;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends Thread {

    private String filePath;

    private static int userId = 1;

    public Main(String file) {
        filePath = file;
    }

    public static void main(String[] args) throws IOException {

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(
                        new FileInputStream("C://Users//Gabriel//Desktop//gps//serviceAccountKey.json"))
                .setDatabaseUrl("https://hereiam-c7f82.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

        final File folder = new File("C://Users//Gabriel//Desktop//gps//routes");
        listFilesForFolder(folder);
    }

    public static void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                (new Main(fileEntry.getAbsolutePath())).start();
            }
        }
    }


    public void runFile(String filePath) throws IOException {

        String user = "test" + userId++;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ArrayList<Location> points = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            String line = br.readLine();

            String name = line;

            line = br.readLine();
            String picture = line;

            line = br.readLine();
            while (line != null) {

                String[] l = line.split(";");


                Double latitude = Double.parseDouble(l[0]);
                Double longitude = Double.parseDouble(l[1]);
                points.add(new Location(latitude, longitude, name, picture));
                line = br.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }

        System.out.println("Carregado: " + filePath);

        for (Location point : points) {
            Map<String, Object> users = new HashMap<>();
            users.put("latitude", point.getLatitude());
            users.put("longitude", point.getLongitude());
            users.put("name", point.getName());
            users.put("picture", point.getPicture());

            System.out.println("Update: " + user + "  " + users);
            database.getReference().child("locations").child(user).setValue(users);

            try {
                int randomNum = 3000 + (int) (Math.random() * 5000);
                Thread.sleep(randomNum);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            System.out.println(this.filePath);
            runFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
