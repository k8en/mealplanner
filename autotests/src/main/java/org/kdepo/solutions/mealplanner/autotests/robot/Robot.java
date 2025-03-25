package org.kdepo.solutions.mealplanner.autotests.robot;

import org.jspecify.annotations.Nullable;
import org.kdepo.solutions.mealplanner.autotests.RobotConstants;
import org.kdepo.solutions.mealplanner.autotests.exceptions.DataMismatchException;
import org.kdepo.solutions.mealplanner.autotests.exceptions.UrlNotLoadedException;
import org.kdepo.solutions.mealplanner.autotests.exceptions.WebElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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

    public void pause(long pause) {
        try {
            Thread.sleep(pause);
        } catch (InterruptedException ignored) {
        }
    }

    public void navigate(String url) {
        System.out.println("[QA] Navigate to url '" + url + "'");
        try {
            driver.get(url);
        } catch (Exception e) {
            throw new UrlNotLoadedException("Error! No access to url '" + url + "'");
        }
    }

    public Integer getIdFromUrl(String url, String text) {
        if (url == null || url.isEmpty()) {
            throw new RuntimeException("Error! Url is not provided!");
        }

        if (text == null || text.isEmpty()) {
            throw new RuntimeException("Error! Url text is not provided!");
        }

        int pos = url.indexOf(text);
        if (pos == -1) {
            throw new RuntimeException("Error! Url text is not found!");
        }

        String valueStr = url.substring(pos + text.length());
        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error! Url id cannot be converted: " + valueStr + "!");
        }

        return value;
    }

    public void sendTextToElement(String webElementId, String textToInput) {
        System.out.println("[QA] Send text to field with id '" + webElementId + "'. Text to send: '" + textToInput + "'");
        WebElement textBox = driver.findElement(By.id(webElementId));
        if (!textBox.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Field not found with id '" + webElementId + "'");
        }
        textBox.clear();
        textBox.sendKeys(textToInput);
    }

    public void clickOnElement(String webElementId) {
        System.out.println("[QA] Click on element with id '" + webElementId + "'");
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Clickable element not found with id '" + webElementId + "'");
        }
        webElement.click();
    }

    public String readTextFromElement(String webElementId) {
        System.out.println("[QA] Read text from element with id '" + webElementId + "'");
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Readable field not found with id '" + webElementId + "'");
        }
        return webElement.getText();
    }

    public void compareTextWithElement(String webElementId, String expectedText) {
        String actualText = readTextFromElement(webElementId);

        if (expectedText == null) {
            if (actualText != null) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: is null. Actual: '" + actualText + "'");
            }

        } else if (expectedText.isEmpty()) {
            if (actualText == null) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: is empty. Actual: is null");

            } else if (!actualText.isEmpty()) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: is empty. Actual: '" + actualText + "'");
            }

        } else {
            if (actualText == null) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: '" + expectedText + "'. Actual is null");

            } else if (actualText.isEmpty()) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: '" + expectedText + "'. Actual is empty");

            } else if (!actualText.equals(expectedText)) {
                throw new DataMismatchException("Error! Web element '" + webElementId + "' text mismatch. Expected: '" + expectedText + "'. Actual: '" + actualText + "'");
            }
        }
    }

    public void selectSingleValueFromList(String webElementId, String value) {
        System.out.println("[QA] Select single value on list element with id '" + webElementId + "', value " + value);
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! List not found with id '" + webElementId + "'");
        }
        Select select = new Select(webElement);
        select.selectByValue("value");
    }

    public String readSingleSelectedValueFromList(String webElementId) {
        System.out.println("[QA] Read single value from list element with id '" + webElementId + "'");
        WebElement webElement = driver.findElement(By.id(webElementId));
        if (!webElement.isDisplayed()) {
            throw new WebElementNotFoundException("Error! List not found with id '" + webElementId + "'");
        }
        Select select = new Select(webElement);
        WebElement selectedWebElement = select.getFirstSelectedOption();
        return selectedWebElement.getText();
    }

    public void login(String username, String password) {
        System.out.println("[QA] Log in with credentials: username='" + username + "', password='" + password + "'");

        String url = serverAddress + "/login";

        navigate(url);
        sendTextToElement("username", username);
        sendTextToElement("password", password);

        clickOnElement("submit");

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

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);

        clickOnElement("submit");
    }

    public Integer getTagIdFromUrl(@Nullable String currentUrl) {
        return getIdFromUrl(currentUrl, "/tags/");
    }

    public void compareTag(Integer tagId, String name, String description) {
        System.out.println("[QA] Compare tag data: tagId=" + tagId + ", name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/" + tagId;

        navigate(url);

        compareTextWithElement("f_name", name);
        compareTextWithElement("f_description", description);
    }

    public void openTagsList() {
        System.out.println("[QA] Open tags list page");

        String url = serverAddress + "/tags";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.TAGS_LIST.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Tags list page not accessible. Actual page title is '" + pageTitle + "'");
        }
    }

    public void updateTag(Integer tagId, String name, String description) {
        System.out.println("[QA] Update tag data with the next parameters: tagId=" + tagId + ", name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/" + tagId + "/update";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.TAG_UPDATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Tag update form not accessible. Actual page title is '" + pageTitle + "'");
        }

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);

        clickOnElement("submit");
    }

    public void deleteTag(Integer tagId) {
        System.out.println("[QA] Delete tag with the next tagId=" + tagId);

        String url = serverAddress + "/tags/" + tagId + "/delete";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.TAG_DELETE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Tag delete form not accessible. Actual page title is '" + pageTitle + "'");
        }

        clickOnElement("submit");
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

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);
        sendTextToElement("f_calories", String.valueOf(calories));
        sendTextToElement("f_proteins", String.valueOf(proteins));
        sendTextToElement("f_fats", String.valueOf(fats));
        sendTextToElement("f_carbs", String.valueOf(carbs));

        clickOnElement("submit");
    }

    public Integer getProductIdFromUrl(@Nullable String currentUrl) {
        return getIdFromUrl(currentUrl, "/products/");
    }

    public void compareProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Compare product data: "
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

        compareTextWithElement("f_name", name);
        compareTextWithElement("f_description", description);
        compareTextWithElement("f_calories", String.valueOf(calories));
        compareTextWithElement("f_proteins", String.valueOf(proteins));
        compareTextWithElement("f_fats", String.valueOf(fats));
        compareTextWithElement("f_carbs", String.valueOf(carbs));
    }

    public void openProductsList() {
        System.out.println("[QA] Open products list page");

        String url = serverAddress + "/products";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.PRODUCTS_LIST.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Products list page not accessible. Actual page title is '" + pageTitle + "'");
        }
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

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);
        sendTextToElement("f_calories", String.valueOf(calories));
        sendTextToElement("f_proteins", String.valueOf(proteins));
        sendTextToElement("f_fats", String.valueOf(fats));
        sendTextToElement("f_carbs", String.valueOf(carbs));

        clickOnElement("submit");
    }

    public void deleteProduct(Integer productId) {
        System.out.println("[QA] Delete product with the next productId=" + productId);

        String url = serverAddress + "/products/" + productId + "/delete";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.PRODUCT_DELETE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Product delete form not accessible. Actual page title is '" + pageTitle + "'");
        }

        clickOnElement("submit");
    }

    public void createRecipe(String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Create new product with the next parameters: "
                + "name='" + name + "'"
                + ", description='" + description + "'"
                + ", source='" + source + "'"
                + ", portions=" + portions
                + ", weight=" + weight
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/recipes/create";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.RECIPE_CREATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Recipe create form not accessible. Actual page title is '" + pageTitle + "'");
        }

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);
        sendTextToElement("f_source", source);
        sendTextToElement("f_portions", String.valueOf(portions));
        sendTextToElement("f_weight", String.valueOf(weight));
        sendTextToElement("f_calories", String.valueOf(calories));
        sendTextToElement("f_proteins", String.valueOf(proteins));
        sendTextToElement("f_fats", String.valueOf(fats));
        sendTextToElement("f_carbs", String.valueOf(carbs));

        clickOnElement("submit");
    }

    public void compareRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Compare product data: "
                + "recipeId=" + recipeId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", source='" + source + "'"
                + ", portions=" + portions
                + ", weight=" + weight
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/recipes/" + recipeId;

        navigate(url);

        compareTextWithElement("f_name", name);
        compareTextWithElement("f_description", description);
        compareTextWithElement("f_source", source);
        compareTextWithElement("f_portions", String.valueOf(portions));
        compareTextWithElement("f_weight", String.valueOf(weight));
        compareTextWithElement("f_calories", String.valueOf(calories));
        compareTextWithElement("f_proteins", String.valueOf(proteins));
        compareTextWithElement("f_fats", String.valueOf(fats));
        compareTextWithElement("f_carbs", String.valueOf(carbs));
    }

    public void openRecipesList() {
        System.out.println("[QA] Open recipes list page");

        String url = serverAddress + "/recipes";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.RECIPES_LIST.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Recipes list page not accessible. Actual page title is '" + pageTitle + "'");
        }
    }

    public void updateRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Update recipe parameters with the next data: "
                + "recipeId=" + recipeId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", source='" + source + "'"
                + ", portions=" + portions
                + ", weight=" + weight
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        String url = serverAddress + "/recipes/" + recipeId + "/update";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.RECIPE_UPDATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Recipe update form not accessible. Actual page title is '" + pageTitle + "'");
        }

        sendTextToElement("f_name", name);
        sendTextToElement("f_description", description);
        sendTextToElement("f_source", source);
        sendTextToElement("f_portions", String.valueOf(portions));
        sendTextToElement("f_weight", String.valueOf(weight));
        sendTextToElement("f_calories", String.valueOf(calories));
        sendTextToElement("f_proteins", String.valueOf(proteins));
        sendTextToElement("f_fats", String.valueOf(fats));
        sendTextToElement("f_carbs", String.valueOf(carbs));

        clickOnElement("submit");
    }

    public void deleteRecipe(Integer recipeId) {
        System.out.println("[QA] Delete recipe with the next recipeId=" + recipeId);

        String url = serverAddress + "/recipes/" + recipeId + "/delete";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.RECIPE_DELETE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Recipe delete form not accessible. Actual page title is '" + pageTitle + "'");
        }

        clickOnElement("submit");
    }

    public void createIngredient(Integer recipeId, String name, Integer productId, Integer amount, Integer unitId) {
        System.out.println("[QA] Create new ingredient with the next parameters: "
                + "recipeId='" + recipeId
                + ", name='" + name + "'"
                + ", productId='" + productId
                + ", amount='" + amount
                + ", unitId='" + unitId
        );

        String url = serverAddress + "/ingredients/create?recipe_id=" + recipeId;

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.INGREDIENT_CREATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Ingredient create form not accessible. Actual page title is '" + pageTitle + "'");
        }

        sendTextToElement("f_name", name);
        selectSingleValueFromList("f_product", String.valueOf(productId));
        sendTextToElement("f_amount", String.valueOf(amount));
        selectSingleValueFromList("f_unit", String.valueOf(unitId));

        clickOnElement("submit");
    }

    public boolean compareIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        System.out.println("[QA] Compare ingredient data: "
                + "ingredientId=" + ingredientId
                + ", name='" + name + "'"
                + ", recipeId=" + recipeId
                + ", productId=" + productId
                + ", amount=" + amount
                + ", unitId=" + unitId
        );

        String url = serverAddress + "/ingredients/" + ingredientId;

        navigate(url);

        String ingredientName = readTextFromElement("f_name");
        if (ingredientName == null) {
            System.out.println("[QA] Not able to read ingredient name");
            return false;
        } else if (!name.equals(ingredientName)) {
            System.out.println("[QA] Ingredient name mismatch. Expected: '" + name + "' Actual: '" + ingredientName + "'");
            return false;
        }

        String ingredientProductId = readTextFromElement("f_product");
        if (ingredientProductId == null) {
            System.out.println("[QA] Not able to read ingredient productId");
            return false;
        } else if (!productId.toString().equals(ingredientProductId)) {
            System.out.println("[QA] Ingredient productId mismatch. Expected: '" + productId + "' Actual: '" + ingredientProductId + "'");
            return false;
        }

        String ingredientAmount = readTextFromElement("f_amount");
        if (ingredientAmount == null) {
            System.out.println("[QA] Not able to read ingredient amount");
            return false;
        } else if (!amount.toString().equals(ingredientAmount)) {
            System.out.println("[QA] Ingredient amount mismatch. Expected: '" + amount + "' Actual: '" + ingredientAmount + "'");
            return false;
        }

        String ingredientUnitId = readTextFromElement("f_unit");
        if (ingredientUnitId == null) {
            System.out.println("[QA] Not able to read ingredient unitId");
            return false;
        } else if (!unitId.toString().equals(ingredientUnitId)) {
            System.out.println("[QA] Ingredient unitId mismatch. Expected: '" + unitId + "' Actual: '" + ingredientUnitId + "'");
            return false;
        }

        return true;
    }

    public void updateIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        System.out.println("[QA] Update ingredient parameters with the next data: "
                + "ingredientId=" + ingredientId
                + ", name='" + name + "'"
                + ", recipeId=" + recipeId
                + ", productId=" + productId
                + ", amount=" + amount
                + ", unitId=" + unitId
        );

        String url = serverAddress + "/ingredients/" + ingredientId + "/update";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.INGREDIENT_UPDATE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Ingredient update form not accessible. Actual page title is '" + pageTitle + "'");
        }

        sendTextToElement("f_name", name);
        selectSingleValueFromList("f_product", String.valueOf(productId));
        sendTextToElement("f_amount", String.valueOf(amount));
        selectSingleValueFromList("f_unit", String.valueOf(unitId));

        clickOnElement("submit");
    }

    public void deleteIngredient(Integer ingredientId) {
        System.out.println("[QA] Delete ingredient with the next ingredientId=" + ingredientId);

        String url = serverAddress + "/ingredients/" + ingredientId + "/delete";

        navigate(url);

        String pageTitle = driver.getTitle();
        if (!RobotConstants.PageTitle.INGREDIENT_DELETE.equals(pageTitle)) {
            throw new UrlNotLoadedException("Error! Ingredient delete form not accessible. Actual page title is '" + pageTitle + "'");
        }

        clickOnElement("submit");
    }
}
