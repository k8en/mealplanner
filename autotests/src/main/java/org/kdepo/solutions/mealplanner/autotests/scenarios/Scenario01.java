package org.kdepo.solutions.mealplanner.autotests.scenarios;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;

public class Scenario01 {

    /**
     * Access to links as anonymous user
     */
    public static void execute(Robot robot) {
        System.out.println("[QA] Started scenario 01");
        robot.openTagsList();
        robot.pause(1000);
        robot.openProductsList();
        robot.pause(1000);
        robot.openRecipesList();
        robot.pause(1000);
        System.out.println("[QA] Completed scenario 01");
    }
}
