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
}
