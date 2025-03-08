package org.kdepo.solutions.mealplanner.service;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class OperationsLogService {

    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final StringBuilder builder;

    public OperationsLogService() {
        builder = new StringBuilder();
    }

    public void register(String user, String operation, String objectType, String oldData, String newData) {
        System.out.println(
                DATE_FORMAT.format(new Date())
                        + " user=" + user
                        + " operation=" + operation
                        + " object=" + objectType
                        + " old={" + oldData + "}"
                        + " new={" + newData + "}"
        );

    }

    public void registerProductCreated(String userName, Product product) {
        builder.setLength(0);
        builder.append("product_id=").append(product.getProductId());
        builder.append(", name=").append(product.getName());
        builder.append(", description=").append(product.getDescription());
        builder.append(", calories=").append(product.getCalories());
        builder.append(", proteins=").append(product.getProteins());
        builder.append(", fats=").append(product.getFats());
        builder.append(", carbs=").append(product.getCarbs());

        register(userName, "C", "PRODUCT", null, builder.toString());
    }

    public void registerProductUpdated(String userName, Product oldData, @Valid Product newData) {
        builder.setLength(0);
        builder.append("product_id=").append(oldData.getProductId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", description=").append(oldData.getDescription());
        builder.append(", calories=").append(oldData.getCalories());
        builder.append(", proteins=").append(oldData.getProteins());
        builder.append(", fats=").append(oldData.getFats());
        builder.append(", carbs=").append(oldData.getCarbs());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("product_id=").append(newData.getProductId());
        builder.append(", name=").append(newData.getName());
        builder.append(", description=").append(newData.getDescription());
        builder.append(", calories=").append(newData.getCalories());
        builder.append(", proteins=").append(newData.getProteins());
        builder.append(", fats=").append(newData.getFats());
        builder.append(", carbs=").append(newData.getCarbs());
        String newDataValue = builder.toString();

        register(userName, "U", "PRODUCT", oldDataValue, newDataValue);
    }

    public void registerProductDeleted(String userName, Integer productId) {
        register(userName, "D", "PRODUCT", String.valueOf(productId), null);
    }

    public void registerTagCreated(String userName, Tag tag) {
        builder.setLength(0);
        builder.append("tag_id=").append(tag.getTagId());
        builder.append(", name=").append(tag.getName());
        builder.append(", description=").append(tag.getDescription());

        register(userName, "C", "TAG", null, builder.toString());
    }

    public void registerTagUpdated(String userName, Tag oldData, @Valid Tag newData) {
        builder.setLength(0);
        builder.append("product_id=").append(oldData.getTagId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", description=").append(oldData.getDescription());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("product_id=").append(newData.getTagId());
        builder.append(", name=").append(newData.getName());
        builder.append(", description=").append(newData.getDescription());
        String newDataValue = builder.toString();

        register(userName, "U", "TAG", oldDataValue, newDataValue);
    }

    public void registerTagDeleted(String userName, Integer tagId) {
        register(userName, "D", "TAG", String.valueOf(tagId), null);
    }

    public void registerTagSet(String userName, Integer tagId, Integer recipeId) {
        register(userName, "U", "TAG", null, String.valueOf(tagId) + " SET TO " + String.valueOf(recipeId));
    }

    public void registerTagUnset(String userName, Integer tagId, Integer recipeId) {
        register(userName, "U", "TAG", null, String.valueOf(tagId) + " UNSET FROM " + String.valueOf(recipeId));
    }

    public void registerRecipeCreated(String userName, Recipe recipe) {
        builder.setLength(0);
        builder.append("recipe_id=").append(recipe.getRecipeId());
        builder.append(", name=").append(recipe.getName());
        builder.append(", description=").append(recipe.getDescription());
        builder.append(", source=").append(recipe.getSource());
        builder.append(", portions=").append(recipe.getPortions());
        builder.append(", weight=").append(recipe.getWeight());
        builder.append(", calories=").append(recipe.getCalories());
        builder.append(", proteins=").append(recipe.getProteins());
        builder.append(", fats=").append(recipe.getFats());
        builder.append(", carbs=").append(recipe.getCarbs());

        register(userName, "C", "RECIPE", null, builder.toString());
    }

    public void registerRecipeUpdated(String userName, Recipe oldData, @Valid Recipe newData) {
        builder.setLength(0);
        builder.append("recipe_id=").append(oldData.getRecipeId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", description=").append(oldData.getDescription());
        builder.append(", source=").append(oldData.getSource());
        builder.append(", portions=").append(oldData.getPortions());
        builder.append(", weight=").append(oldData.getWeight());
        builder.append(", calories=").append(oldData.getCalories());
        builder.append(", proteins=").append(oldData.getProteins());
        builder.append(", fats=").append(oldData.getFats());
        builder.append(", carbs=").append(oldData.getCarbs());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("recipe_id=").append(newData.getRecipeId());
        builder.append(", name=").append(newData.getName());
        builder.append(", description=").append(newData.getDescription());
        builder.append(", source=").append(newData.getSource());
        builder.append(", portions=").append(newData.getPortions());
        builder.append(", weight=").append(newData.getWeight());
        builder.append(", calories=").append(newData.getCalories());
        builder.append(", proteins=").append(newData.getProteins());
        builder.append(", fats=").append(newData.getFats());
        builder.append(", carbs=").append(newData.getCarbs());
        String newDataValue = builder.toString();

        register(userName, "U", "RECIPE", oldDataValue, newDataValue);
    }

    public void registerRecipeDeleted(String userName, Integer recipeId) {
        register(userName, "D", "RECIPE", String.valueOf(recipeId), null);
    }

    public void registerIngredientCreated(String userName, Ingredient ingredient) {
        builder.setLength(0);
        builder.append("ingredient_id=").append(ingredient.getIngredientId());
        builder.append(", name=").append(ingredient.getName());
        builder.append(", recipe_id=").append(ingredient.getRecipeId());
        builder.append(", product_id=").append(ingredient.getProductId());
        builder.append(", amount=").append(ingredient.getAmount());
        builder.append(", unit_id=").append(ingredient.getUnitId());

        register(userName, "C", "INGREDIENT", null, builder.toString());
    }

    public void registerIngredientUpdated(String userName, Ingredient oldData, @Valid Ingredient newData) {
        builder.setLength(0);
        builder.append("ingredient_id=").append(oldData.getIngredientId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", recipe_id=").append(oldData.getRecipeId());
        builder.append(", product_id=").append(oldData.getProductId());
        builder.append(", amount=").append(oldData.getAmount());
        builder.append(", unit_id=").append(oldData.getUnitId());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("ingredient_id=").append(newData.getIngredientId());
        builder.append(", name=").append(newData.getName());
        builder.append(", recipe_id=").append(newData.getRecipeId());
        builder.append(", product_id=").append(newData.getProductId());
        builder.append(", amount=").append(newData.getAmount());
        builder.append(", unit_id=").append(newData.getUnitId());
        String newDataValue = builder.toString();

        register(userName, "U", "INGREDIENT", oldDataValue, newDataValue);
    }

    public void registerIngredientDeleted(String userName, Integer ingredientId) {
        register(userName, "D", "INGREDIENT", String.valueOf(ingredientId), null);
    }
}
