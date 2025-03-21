package org.kdepo.solutions.mealplanner.autotests.robot;

import org.kdepo.solutions.mealplanner.autotests.RobotConstants;
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

    public void navigate(String url) {
        System.out.println("[QA] Navigate to url '" + url + "'");
        try {
            driver.get(url);
        } catch (Exception e) {
            throw new UrlNotLoadedException("Error! No access to url '" + url + "'");
        }
    }

    public void sendTextToElement(String webElementId, String textToInput) {
        System.out.println("[QA] Send text to field with id '" + webElementId + "'. Text to send: '" + textToInput + "'");
        WebElement textBox = driver.findElement(By.id(webElementId));
        if (!textBox.isDisplayed()) {
            throw new WebElementNotFoundException("Error! Field not found with id '" + webElementId + "'");
        }
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

    public boolean readTag(Integer tagId, String name, String description) {
        System.out.println("[QA] Read and compare tag data: tagId=" + tagId + ", name='" + name + "', description='" + description + "'");

        String url = serverAddress + "/tags/" + tagId;

        navigate(url);

        String tagName = readTextFromElement("f_name");
        if (tagName == null) {
            System.out.println("[QA] Not able to read tag name");
            return false;
        } else if (!name.equals(tagName)) {
            System.out.println("[QA] Tag name mismatch. Expected: '" + name + "' Actual: '" + tagName + "'");
            return false;
        }

        String tagDescription = readTextFromElement("f_description");
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

        String productName = readTextFromElement("f_name");
        if (productName == null) {
            System.out.println("[QA] Not able to read product name");
            return false;
        } else if (!name.equals(productName)) {
            System.out.println("[QA] Product name mismatch. Expected: '" + name + "' Actual: '" + productName + "'");
            return false;
        }

        String productDescription = readTextFromElement("f_description");
        if (productDescription == null) {
            System.out.println("[QA] Not able to read product description");
            return false;
        } else if (!description.equals(productDescription)) {
            System.out.println("[QA] Product description mismatch. Expected: '" + description + "' Actual: '" + productDescription + "'");
            return false;
        }

        String productCalories = readTextFromElement("f_calories");
        if (productCalories == null) {
            System.out.println("[QA] Not able to read product calories");
            return false;
        } else if (!calories.equals(new BigDecimal(productCalories))) {
            System.out.println("[QA] Product calories mismatch. Expected: '" + calories + "' Actual: '" + productCalories + "'");
            return false;
        }

        String productProteins = readTextFromElement("f_proteins");
        if (productProteins == null) {
            System.out.println("[QA] Not able to read product proteins");
            return false;
        } else if (!proteins.equals(new BigDecimal(productProteins))) {
            System.out.println("[QA] Product proteins mismatch. Expected: '" + proteins + "' Actual: '" + productProteins + "'");
            return false;
        }

        String productFats = readTextFromElement("f_fats");
        if (productFats == null) {
            System.out.println("[QA] Not able to read product fats");
            return false;
        } else if (!fats.equals(new BigDecimal(productFats))) {
            System.out.println("[QA] Product fats mismatch. Expected: '" + fats + "' Actual: '" + productFats + "'");
            return false;
        }

        String productCarbs = readTextFromElement("f_carbs");
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

    public boolean readRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[QA] Read and compare product data: "
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

        String recipeName = readTextFromElement("f_name");
        if (recipeName == null) {
            System.out.println("[QA] Not able to read recipe name");
            return false;
        } else if (!name.equals(recipeName)) {
            System.out.println("[QA] Recipe name mismatch. Expected: '" + name + "' Actual: '" + recipeName + "'");
            return false;
        }

        String recipeDescription = readTextFromElement("f_description");
        if (recipeDescription == null) {
            System.out.println("[QA] Not able to read recipe description");
            return false;
        } else if (!description.equals(recipeDescription)) {
            System.out.println("[QA] Recipe description mismatch. Expected: '" + description + "' Actual: '" + recipeDescription + "'");
            return false;
        }

        String recipeSource = readTextFromElement("f_source");
        if (recipeSource == null) {
            System.out.println("[QA] Not able to read recipe source");
            return false;
        } else if (!source.equals(recipeSource)) {
            System.out.println("[QA] Recipe source mismatch. Expected: '" + name + "' Actual: '" + recipeSource + "'");
            return false;
        }

        String recipePortions = readTextFromElement("f_portions");
        if (recipePortions == null) {
            System.out.println("[QA] Not able to read recipe portions");
            return false;
        } else if (!portions.equals(Integer.parseInt(recipePortions))) {
            System.out.println("[QA] Recipe portions mismatch. Expected: '" + portions + "' Actual: '" + recipePortions + "'");
            return false;
        }

        String recipeWeight = readTextFromElement("f_weight");
        if (recipeWeight == null) {
            System.out.println("[QA] Not able to read recipe weight");
            return false;
        } else if (!weight.equals(new BigDecimal(recipeWeight))) {
            System.out.println("[QA] Recipe weight mismatch. Expected: '" + weight + "' Actual: '" + recipeWeight + "'");
            return false;
        }

        String recipeCalories = readTextFromElement("f_calories");
        if (recipeCalories == null) {
            System.out.println("[QA] Not able to read recipe calories");
            return false;
        } else if (!calories.equals(new BigDecimal(recipeCalories))) {
            System.out.println("[QA] Recipe calories mismatch. Expected: '" + calories + "' Actual: '" + recipeCalories + "'");
            return false;
        }

        String recipeProteins = readTextFromElement("f_proteins");
        if (recipeProteins == null) {
            System.out.println("[QA] Not able to read recipe proteins");
            return false;
        } else if (!proteins.equals(new BigDecimal(recipeProteins))) {
            System.out.println("[QA] Recipe proteins mismatch. Expected: '" + proteins + "' Actual: '" + recipeProteins + "'");
            return false;
        }

        String recipeFats = readTextFromElement("f_fats");
        if (recipeFats == null) {
            System.out.println("[QA] Not able to read recipe fats");
            return false;
        } else if (!fats.equals(new BigDecimal(recipeFats))) {
            System.out.println("[QA] Recipe fats mismatch. Expected: '" + fats + "' Actual: '" + recipeFats + "'");
            return false;
        }

        String recipeCarbs = readTextFromElement("f_carbs");
        if (recipeCarbs == null) {
            System.out.println("[QA] Not able to read recipe carbs");
            return false;
        } else if (!carbs.equals(new BigDecimal(recipeCarbs))) {
            System.out.println("[QA] Recipe carbs mismatch. Expected: '" + carbs + "' Actual: '" + recipeCarbs + "'");
            return false;
        }

        return true;
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

    public boolean readIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        System.out.println("[QA] Read and compare ingredient data: "
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
