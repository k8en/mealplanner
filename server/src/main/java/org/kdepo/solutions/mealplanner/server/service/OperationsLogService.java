package org.kdepo.solutions.mealplanner.server.service;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.model.InstructionStep;
import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.model.Menu;
import org.kdepo.solutions.mealplanner.shared.model.Product;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OperationsLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsLogService.class);

    private final StringBuilder builder;

    public OperationsLogService() {
        builder = new StringBuilder();
    }

    public void register(String user, String operation, String objectType, String oldData, String newData) {
        LOGGER.trace("[OLS] operation={}, objectType={}, user={}, oldData=[{}], newData=[{}]",
                operation, objectType, user, oldData, newData
        );
        //TODO save into database
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
        builder.append(", instructionTypeId=").append(recipe.getInstructionTypeId());
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
        builder.append(", instructionTypeId=").append(oldData.getInstructionTypeId());
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
        builder.append(", instructionTypeId=").append(newData.getInstructionTypeId());
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

    public void registerMenuCreated(String userName, Menu menu) {
        builder.setLength(0);
        builder.append("menu_id=").append(menu.getMenuId());
        builder.append(", menu_type_id=").append(menu.getMenuTypeId());
        builder.append(", name=").append(menu.getName());
        builder.append(", active=").append(menu.getActive());

        register(userName, "C", "MENU", null, builder.toString());
    }

    public void registerMenuUpdated(String userName, Menu oldData, @Valid Menu newData) {
        builder.setLength(0);
        builder.append("menu_id=").append(oldData.getMenuId());
        builder.append(", menu_type_id=").append(oldData.getMenuTypeId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", active=").append(oldData.getActive());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("menu_id=").append(newData.getMenuId());
        builder.append(", menu_type_id=").append(newData.getMenuTypeId());
        builder.append(", name=").append(newData.getName());
        builder.append(", active=").append(newData.getActive());
        String newDataValue = builder.toString();

        register(userName, "U", "MENU", oldDataValue, newDataValue);
    }

    public void registerMenuDeleted(String userName, Integer menuId) {
        register(userName, "D", "MENU", String.valueOf(menuId), null);
    }

    public void registerDayCreated(String userName, Day day) {
        builder.setLength(0);
        builder.append("day_id=").append(day.getDayId());
        builder.append(", menu_id=").append(day.getMenuId());
        builder.append(", week_id=").append(day.getWeekId());
        builder.append(", name=").append(day.getName());
        builder.append(", order_number=").append(day.getOrderNumber());

        register(userName, "C", "DAY", null, builder.toString());
    }

    public void registerDayUpdated(String userName, Day oldData, @Valid Day newData) {
        builder.setLength(0);
        builder.append("day_id=").append(oldData.getDayId());
        builder.append(", menu_id=").append(oldData.getMenuId());
        builder.append(", week_id=").append(oldData.getWeekId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", order_number=").append(oldData.getOrderNumber());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("day_id=").append(newData.getDayId());
        builder.append(", menu_id=").append(newData.getMenuId());
        builder.append(", week_id=").append(newData.getWeekId());
        builder.append(", name=").append(newData.getName());
        builder.append(", order_number=").append(newData.getOrderNumber());
        String newDataValue = builder.toString();

        register(userName, "U", "DAY", oldDataValue, newDataValue);
    }

    public void registerDayDeleted(String userName, Integer dayId) {
        register(userName, "D", "DAY", String.valueOf(dayId), null);
    }

    public void registerWeekCreated(String userName, Week week) {
        builder.setLength(0);
        builder.append("week_id=").append(week.getWeekId());
        builder.append(", menu_id=").append(week.getMenuId());
        builder.append(", name=").append(week.getName());
        builder.append(", order_number=").append(week.getOrderNumber());

        register(userName, "C", "WEEK", null, builder.toString());
    }

    public void registerWeekUpdated(String userName, Week oldData, @Valid Week newData) {
        builder.setLength(0);
        builder.append("week_id=").append(oldData.getWeekId());
        builder.append(", menu_id=").append(oldData.getMenuId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", order_number=").append(oldData.getOrderNumber());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("week_id=").append(newData.getWeekId());
        builder.append(", menu_id=").append(newData.getMenuId());
        builder.append(", name=").append(newData.getName());
        builder.append(", order_number=").append(newData.getOrderNumber());
        String newDataValue = builder.toString();

        register(userName, "U", "WEEK", oldDataValue, newDataValue);
    }

    public void registerWeekDeleted(String userName, Integer weekId) {
        register(userName, "D", "WEEK", String.valueOf(weekId), null);
    }

    public void registerMealCreated(String userName, Meal meal) {
        builder.setLength(0);
        builder.append("meal_id=").append(meal.getMealId());
        builder.append(", day_id=").append(meal.getDayId());
        builder.append(", name=").append(meal.getName());
        builder.append(", order_number=").append(meal.getOrderNumber());

        register(userName, "C", "MEAL", null, builder.toString());
    }

    public void registerMealUpdated(String userName, Meal oldData, @Valid Meal newData) {
        builder.setLength(0);
        builder.append("meal_id=").append(oldData.getMealId());
        builder.append(", day_id=").append(oldData.getDayId());
        builder.append(", name=").append(oldData.getName());
        builder.append(", order_number=").append(oldData.getOrderNumber());
        String oldDataValue = builder.toString();

        builder.setLength(0);
        builder.append("meal_id=").append(newData.getMealId());
        builder.append(", day_id=").append(newData.getDayId());
        builder.append(", name=").append(newData.getName());
        builder.append(", order_number=").append(newData.getOrderNumber());
        String newDataValue = builder.toString();

        register(userName, "U", "MEAL", oldDataValue, newDataValue);
    }

    public void registerMealDeleted(String userName, Integer mealId) {
        register(userName, "D", "MEAL", String.valueOf(mealId), null);
    }

    public void registerInstructionStepCreated(String userName, InstructionStep instructionStepCreated) {
        builder.setLength(0);
        builder.append("instructionStepId=").append(instructionStepCreated.getInstructionStepId());
        builder.append(", recipeId=").append(instructionStepCreated.getRecipeId());
        builder.append(", name=").append(instructionStepCreated.getName());
        builder.append(", description=").append(instructionStepCreated.getDescription());
        builder.append(", image=").append(instructionStepCreated.getImage());
        builder.append(", orderNumber=").append(instructionStepCreated.getOrderNumber());

        register(userName, "C", "STEP", null, builder.toString());
    }
}
