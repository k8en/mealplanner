package org.kdepo.solutions.mealplanner.autotests;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;
import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public class Launcher {

    public static void main(String[] args) throws IOException {
        // Calculate root folder
        String rootFolder = null;
        try {
            rootFolder = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            rootFolder = rootFolder + File.separator;
        } catch (URISyntaxException e) {
            System.out.println("[QA] Error in root folder calculation!");
            e.printStackTrace();
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(rootFolder + "autotests.properties"));
        String server = properties.getProperty("server");
        System.out.println("[QA] server=" + server);

        // Prepare browser driver
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000));

        // Prepare QA robot
        Robot robot = Robot.getInstance();
        robot.setServerAddress(server);
        robot.setWebDriver(driver);

        // Check list pages as anon
        robot.openTagsList();
        robot.pause(1000);
        robot.openProductsList();
        robot.pause(1000);
        robot.openRecipesList();
        robot.pause(1000);

        // Login as user
        robot.login("user", "password");
        robot.pause(1000);

        // Check list pages as user
        robot.openTagsList();
        robot.pause(1000);
        robot.openProductsList();
        robot.pause(1000);
        robot.openRecipesList();
        robot.pause(1000);

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("[QA] Test key is generated as " + uuid);

        // Tag manipulation
        // 1. Create new user tag
        Tag userTag = new Tag();
        userTag.setName("Метка_" + uuid);
        userTag.setDescription("Описание_" + uuid);
        robot.createTag(userTag.getName(), userTag.getDescription());
        robot.pause(1000);
        Integer userTagId = robot.getTagIdFromUrl(driver.getCurrentUrl());
        userTag.setTagId(userTagId);

        // 2. Check that created as expected
        robot.compareTag(userTag.getTagId(), userTag.getName(), userTag.getDescription());
        robot.pause(1000);

        // 3. Update tag
        userTag.setName(userTag.getName() + "_edited");
        userTag.setDescription(userTag.getDescription() + "_edited");
        robot.updateTag(userTag.getTagId(), userTag.getName(), userTag.getDescription());
        robot.pause(1000);

        // 4. Check that updated as expected
        robot.compareTag(userTag.getTagId(), userTag.getName(), userTag.getDescription());
        robot.pause(1000);

        // 5. Delete tag
        robot.deleteTag(userTag.getTagId());

        System.out.println("[QA] Completed");
    }
}
