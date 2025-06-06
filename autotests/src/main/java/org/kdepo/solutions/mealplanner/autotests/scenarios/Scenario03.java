package org.kdepo.solutions.mealplanner.autotests.scenarios;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;

import java.util.UUID;

public class Scenario03 {

    /**
     * Scenario steps:
     * - login
     * - create product PA
     * - create product PB
     * - create product PC
     * - create recipe RA
     * - create ingredient A for recipe RA-IA
     * - create ingredient B for recipe RA-IB
     * - create ingredient C for recipe RA-IC
     * - create tag TA
     * - create tag TB
     * - set tag TA on RA from recipe bulk set
     * - set tag TB on RA from tag-to-recipe set
     * - logout
     *
     * @param robot
     * @param username
     * @param password
     */
    public static void execute(Robot robot, String username, String password) {
        System.out.println("[QA] Started scenario 03");

        // Generate unique key
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("[QA] Test key is generated as " + uuid);
        System.out.println();

        // Login as user
        robot.login(username, password);
        robot.pause(1000);


        // Logout
        robot.logout();

        System.out.println("[QA] Completed scenario 03");
    }
}
