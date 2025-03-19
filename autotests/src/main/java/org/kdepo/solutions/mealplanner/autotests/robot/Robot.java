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

    public String readWebElement(String webElementId) {
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            System.out.println("[QA] Web element not found with id '" + webElementId + "'");
            return null;
        }
        return webElement.getText();
    }

    public boolean login(String username, String password) {
        String url = serverAddress + "/login";

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open login page: '" + url + "'");
            return false;
        }

        if (!inputTextBox("username", username)) {
            System.out.println("[QA] Not able to provide login data: '" + username + "'");
            return false;
        }

        if (!inputTextBox("password", password)) {
            System.out.println("[QA] Not able to provide password data: '" + password + "'");
            return false;
        }

        if (!clickWebElement("submit")) {
            System.out.println("[QA] Not able to submit login form");
            return false;
        }

        WebElement logout = driver.findElement(By.id("logout"));
        if (!logout.isDisplayed()) {
            System.out.println("[QA] Not able to log in with login '" + username + "'");
        }

        return true;
    }

    public boolean createTag(String name, String description) {
        String url = serverAddress + "/tags/create";

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open tag create page");
            return false;
        }

        String pageTitle = driver.getTitle();
        if (!"Добавление новой метки".equals(pageTitle)) {
            System.out.println("[QA] Tag create form not accessible. Actual page title is '" + pageTitle + "'");
            return false;
        }

        if (!inputTextBox("f_name", name)) {
            System.out.println("[QA] Not able to provide tag name data for create");
            return false;
        }

        if (!inputTextBox("f_description", description)) {
            System.out.println("[QA] Not able to provide tag description data for create");
            return false;
        }

        if (!clickWebElement("submit")) {
            System.out.println("[QA] Not able to submit tag creation form");
            return false;
        }

        return true;
    }

    public boolean readTag(Integer tagId, String name, String description) {
        String url = serverAddress + "/tags/" + tagId;

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open tag details page");
            return false;
        }

        String tagName = readWebElement("f_name");
        if (tagName == null) {
            System.out.println("[QA] Not able to read tag name");
            return false;
        } else if (!name.equals(tagName)) {
            System.out.println("[QA] Tag name mismatch. Expected: '" + name + "' Actual: '" + tagName + "'");
            return false;
        }

        String tagDescription = readWebElement("f_description");
        if (tagDescription == null) {
            System.out.println("[QA] Not able to read tag description");
            return false;
        } else if (!description.equals(tagDescription)) {
            System.out.println("[QA] Tag description mismatch. Expected: '" + description + "' Actual: '" + tagDescription + "'");
            return false;
        }

        return true;
    }

    public boolean updateTag(Integer tagId, String name, String description) {
        String url = serverAddress + "/tags/" + tagId + "/update";

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open tag update page");
            return false;
        }

        String pageTitle = driver.getTitle();
        if (!"Редактирование информации о метке".equals(pageTitle)) {
            System.out.println("[QA] Tag update form not accessible. Actual page title is '" + pageTitle + "'");
            return false;
        }

        if (!inputTextBox("f_name", name)) {
            System.out.println("[QA] Not able to provide tag name data for update");
            return false;
        }

        if (!inputTextBox("f_description", description)) {
            System.out.println("[QA] Not able to provide tag description data for update");
            return false;
        }

        if (!clickWebElement("submit")) {
            System.out.println("[QA] Not able to submit tag creation form");
            return false;
        }

        return true;
    }

    public boolean deleteTag(Integer tagId) {
        String url = serverAddress + "/tags/" + tagId + "/delete";

        if (!navigate(url)) {
            System.out.println("[QA] Not able to open tag delete page");
            return false;
        }

        if (!clickWebElement("submit")) {
            System.out.println("[QA] Not able to submit tag creation form");
            return false;
        }

        return true;
    }
}
