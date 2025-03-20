package org.kdepo.solutions.mealplanner.autotests;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Properties;

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
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));

        // Prepare QA robot
        Robot robot = Robot.getInstance();
        robot.setServerAddress(server);
        robot.setWebDriver(driver);

        robot.login("user", "password");
    }
}
