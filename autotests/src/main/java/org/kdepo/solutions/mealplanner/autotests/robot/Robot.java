package org.kdepo.solutions.mealplanner.autotests.robot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Robot {

    private static Robot instance;

    private WebDriver driver;

    private String serverAddress;

    public static Robot getInstance() {
        if (instance == null) {
            instance = new Robot();
        }
        return instance;
    }

    private Robot() {

    }

    public void setWebDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public boolean navigate(String url) {
        try {
            driver.get(url);
        } catch (Exception e) {
            System.out.println("[QA] Error access to url '" + url + "'");
            return false;
        }
        return true;
    }

    public boolean inputTextBox(String webElementId, String textToInput) {
        WebElement textBox = driver.findElement(By.id(webElementId));
        if (!textBox.isDisplayed()) {
            System.out.println("[QA] Input field not found with id '" + webElementId + "'");
            return false;
        }
        textBox.sendKeys(textToInput);
        return true;
    }

    public boolean clickWebElement(String webElementId) {
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            System.out.println("[QA] Input field not found with id '" + webElementId + "'");
            return false;
        }
        webElement.click();
        return true;
    }

    public boolean login(String user, String password) {
        String url = serverAddress + "/login";

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open login page");
            return false;
        }

        if (!inputTextBox("username", user)) {
            System.out.println("[QA] Not able to provide login data");
            return false;
        }

        if (!inputTextBox("password", password)) {
            System.out.println("[QA] Not able to provide password data");
            return false;
        }

        if (!clickWebElement("submit")) {
            System.out.println("[QA] Not able to submit login form");
            return false;
        }

        WebElement logout = driver.findElement(By.id("logout"));
        if (!logout.isDisplayed()) {
            System.out.println("[QA] Not able to log in with login '" + user + "'");
        }

        return true;
    }
}
