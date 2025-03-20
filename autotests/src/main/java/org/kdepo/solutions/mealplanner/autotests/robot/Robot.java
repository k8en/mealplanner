package org.kdepo.solutions.mealplanner.autotests.robot;

import org.kdepo.solutions.mealplanner.autotests.RobotConstants;
import org.kdepo.solutions.mealplanner.autotests.exceptions.UrlNotLoadedException;
import org.kdepo.solutions.mealplanner.autotests.exceptions.WebElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;

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

    public void navigate(String url) {
        System.out.println("[QA] Navigate to url '" + url + "'");
        try {
            driver.get(url);
        } catch (Exception e) {
            throw new UrlNotLoadedException("Error! No access to url '" + url + "'");
        }
    }

    public void inputTextBox(String webElementId, String textToInput) {
        System.out.println("[QA] Send text to field with id '" + webElementId + "'. Text to send: '" + textToInput + "'");
        WebElement textBox = driver.findElement(By.id(webElementId));
        if (!textBox.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Field not found with id '" + webElementId + "'");
        }
        textBox.sendKeys(textToInput);
    }

    public void clickWebElement(String webElementId) {
        System.out.println("[QA] Click on element with id '" + webElementId + "'");
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Clickable element not found with id '" + webElementId + "'");
        }
        webElement.click();
    }

    public String readWebElement(String webElementId) {
        System.out.println("[QA] Read text from element with id '" + webElementId + "'");
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Readable field not found with id '" + webElementId + "'");
        }
        return webElement.getText();
    }

    public void login(String username, String password) {
        System.out.println("[QA] Log in with credentials: username='" + username + "', password='" + password + "'");

        String url = serverAddress + "/login";

        navigate(url);
        inputTextBox("username", username);
        inputTextBox("password", password);

        clickWebElement("submit");

        WebElement logout = driver.findElement(By.id("logout"));
        if (!logout.isDisplayed()) {
            System.out.println("[QA] Not able to log in with login '" + username + "'");
        }
    }

    public void createTag(String name, String description) {
        System.out.println("[QA] Create new tag with the next parameters: name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/create";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.TAG_CREATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Tag create form not accessible. Actual page title is '" + pageTitle + "'");
        }

        inputTextBox("f_name", name);
        inputTextBox("f_description", description);

        clickWebElement("submit");
    }

    public boolean readTag(Integer tagId, String name, String description) {
        System.out.println("[QA] Read and compare tag data: tagId=" + tagId + ", name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/" + tagId;

        navigate(url);

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

    public void updateTag(Integer tagId, String name, String description) {
        System.out.println("[QA] Update tag data with the next parameters: tagId=" + tagId + ", name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/" + tagId + "/update";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.TAG_UPDATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Tag update form not accessible. Actual page title is '" + pageTitle + "'");
        }

        inputTextBox("f_name", name);
        inputTextBox("f_description", description);

        clickWebElement("submit");
    }

    public void deleteTag(Integer tagId) {
        System.out.println("[QA] Delete tag with the next tagId=" + tagId);

        String url = serverAddress + "/tags/" + tagId + "/delete";

        navigate(url);

        clickWebElement("submit");
    }

    public void createProduct(String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Create new tag with the next parameters: "
                + "name='" + name + "'"
                + ", description='" + description + "'"
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/products/create";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.PRODUCT_CREATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Product create form not accessible. Actual page title is '" + pageTitle + "'");
        }

        inputTextBox("f_name", name);
        inputTextBox("f_description", description);
        inputTextBox("f_calories", String.valueOf(calories));
        inputTextBox("f_proteins", String.valueOf(proteins));
        inputTextBox("f_fats", String.valueOf(fats));
        inputTextBox("f_carbs", String.valueOf(carbs));

        clickWebElement("submit");
    }

    public boolean readProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Read and compare product data: "
                + "productId=" + productId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/products/" + productId;

        navigate(url);

        String productName = readWebElement("f_name");
        if (productName == null) {
            System.out.println("[QA] Not able to read product name");
            return false;
        } else if (!name.equals(productName)) {
            System.out.println("[QA] Product name mismatch. Expected: '" + name + "' Actual: '" + productName + "'");
            return false;
        }

        String productDescription = readWebElement("f_description");
        if (productDescription == null) {
            System.out.println("[QA] Not able to read product description");
            return false;
        } else if (!description.equals(productDescription)) {
            System.out.println("[QA] Product description mismatch. Expected: '" + description + "' Actual: '" + productDescription + "'");
            return false;
        }

        String productCalories = readWebElement("f_calories");
        if (productCalories == null) {
            System.out.println("[QA] Not able to read product calories");
            return false;
        } else if (!calories.equals(new BigDecimal(productCalories))) {
            System.out.println("[QA] Product calories mismatch. Expected: '" + calories + "' Actual: '" + productCalories + "'");
            return false;
        }

        String productProteins = readWebElement("f_proteins");
        if (productProteins == null) {
            System.out.println("[QA] Not able to read product proteins");
            return false;
        } else if (!proteins.equals(new BigDecimal(productProteins))) {
            System.out.println("[QA] Product proteins mismatch. Expected: '" + proteins + "' Actual: '" + productProteins + "'");
            return false;
        }

        String productFats = readWebElement("f_fats");
        if (productFats == null) {
            System.out.println("[QA] Not able to read product fats");
            return false;
        } else if (!fats.equals(new BigDecimal(productFats))) {
            System.out.println("[QA] Product fats mismatch. Expected: '" + fats + "' Actual: '" + productFats + "'");
            return false;
        }

        String productCarbs = readWebElement("f_carbs");
        if (productCarbs == null) {
            System.out.println("[QA] Not able to read product carbs");
            return false;
        } else if (!carbs.equals(new BigDecimal(productCarbs))) {
            System.out.println("[QA] Product carbs mismatch. Expected: '" + carbs + "' Actual: '" + productCarbs + "'");
            return false;
        }

        return true;
    }

    public void updateProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Update product data with the next parameters: "
                + "productId=" + productId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/products/" + productId + "/update";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.PRODUCT_UPDATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Product update form not accessible. Actual page title is '" + pageTitle + "'");
        }

        inputTextBox("f_name", name);
        inputTextBox("f_description", description);
        inputTextBox("f_calories", String.valueOf(calories));
        inputTextBox("f_proteins", String.valueOf(proteins));
        inputTextBox("f_fats", String.valueOf(fats));
        inputTextBox("f_carbs", String.valueOf(carbs));

        clickWebElement("submit");
    }

    public void deleteProduct(Integer productId) {
        System.out.println("[QA] Delete product with the next productId=" + productId);

        String url = serverAddress + "/products/" + productId + "/delete";

        navigate(url);

        clickWebElement("submit");
    }
}
