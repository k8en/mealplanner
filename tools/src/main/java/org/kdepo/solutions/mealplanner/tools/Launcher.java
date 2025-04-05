package org.kdepo.solutions.mealplanner.tools;

import org.kdepo.solutions.mealplanner.tools.db.DbConstructor;

public class Launcher {

    public static void main(String[] args) {
        String pathToDatabase = "";

        System.out.println("[DB] Database creation started");

        DbConstructor dbConstructor = DbConstructor.getInstance();

        boolean isConnected = dbConstructor.connect(pathToDatabase);
        if (!isConnected) {
            System.out.println("[DB] Connection failed. Process stopped");
            return;
        }

        boolean isCreated = dbConstructor.construct(true, true);
        if (!isCreated) {
            System.out.println("[DB] Database creation failed. Process stopped");
            return;
        }

        System.out.println("[DB] Database creation completed");
    }
}
