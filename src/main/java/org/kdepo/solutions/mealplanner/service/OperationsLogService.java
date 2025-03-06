package org.kdepo.solutions.mealplanner.service;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.springframework.stereotype.Service;

@Service
public class OperationsLogService {

    public void registerProductCreated(String userName, Product createdProduct) {

    }

    public void registerProductUpdated(String userName, Product oldProductData, @Valid Product newProductData) {

    }

    public void registerProductDeleted(String userName, Integer productId) {

    }

    public void registerTagCreated(String userName, Tag createdTag) {

    }

    public void registerTagUpdated(String userName, Tag tagFromDb, @Valid Tag tag) {

    }

    public void registerTagDeleted(String userName, Integer tagId) {

    }

    public void registerTagSet(String userName, Integer tagId, Integer recipeId) {

    }

    public void registerTagUnset(String userName, Integer tagId, Integer recipeId) {

    }

    public void registerRecipeCreated(String userName, Recipe createdRecipe) {

    }

    public void registerRecipeUpdated(String userName, Recipe oldRecipeData, @Valid Recipe newRecipeData) {

    }

    public void registerRecipeDeleted(String userName, Integer recipeId) {

    }

    public void registerIngredientDeleted(String userName, Integer ingredientId) {

    }
}
