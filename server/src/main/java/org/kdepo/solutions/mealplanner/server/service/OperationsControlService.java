package org.kdepo.solutions.mealplanner.server.service;

import org.springframework.stereotype.Service;

@Service
public class OperationsControlService {

    public boolean canReadProduct(String userName, Integer productId) {
        return true;
    }

    public boolean canCreateProduct(String userName) {
        return true;
    }

    public boolean canModifyProduct(String userName, Integer productId) {
        return true;
    }

    public boolean canDeleteProduct(String userName, Integer productId) {
        return true;
    }

    public boolean canReadTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canCreateTag(String userName) {
        return true;
    }

    public boolean canModifyTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canDeleteTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canSetTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canUnsetTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canReadRecipe(String userName, Integer recipeId) {
        return true;
    }

    public boolean canCreateRecipe(String userName) {
        return true;
    }

    public boolean canModifyRecipe(String userName, Integer recipeId) {
        return true;
    }

    public boolean canDeleteRecipe(String userName, Integer recipeId) {
        return true;
    }

    public boolean canReadIngredient(String userName, Integer ingredientId) {
        return true;
    }

    public boolean canCreateIngredient(String userName) {
        return true;
    }

    public boolean canModifyIngredient(String userName, Integer ingredientId) {
        return true;
    }

    public boolean canDeleteIngredient(String userName, Integer ingredientId) {
        return true;
    }

    public boolean canReadUnit(String userName, Integer unitId) {
        return true;
    }

    public boolean canReadMenu(String userName, Integer menuId) {
        return true;
    }

    public boolean canCreateMenu(String userName) {
        return true;
    }

    public boolean canModifyMenu(String userName, Integer menuId) {
        return true;
    }

    public boolean canDeleteMenu(String userName, Integer menuId) {
        return true;
    }

    public boolean canReadDay(String userName, Integer dayId) {
        return true;
    }

    public boolean canCreateDay(String userName) {
        return true;
    }

    public boolean canModifyDay(String userName, Integer dayId) {
        return true;
    }

    public boolean canDeleteDay(String userName, Integer dayId) {
        return true;
    }

    public boolean canReadWeek(String userName, Integer weekId) {
        return true;
    }

    public boolean canCreateWeek(String userName) {
        return true;
    }

    public boolean canModifyWeek(String userName, Integer weekId) {
        return true;
    }

    public boolean canDeleteWeek(String userName, Integer weekId) {
        return true;
    }

    public boolean canReadMeal(String userName, Integer mealId) {
        return true;
    }

    public boolean canCreateMeal(String userName) {
        return true;
    }

    public boolean canModifyMeal(String userName, Integer mealId) {
        return true;
    }

    public boolean canDeleteMeal(String userName, Integer mealId) {
        return true;
    }
}
