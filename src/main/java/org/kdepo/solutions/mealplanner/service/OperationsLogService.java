package org.kdepo.solutions.mealplanner.service;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.springframework.stereotype.Service;

@Service
public class OperationsLogService {

    public void registerProductCreated(String userName, Product product) {

    }

    public void registerProductUpdated(String userName, Product oldData, @Valid Product newData) {

    }

    public void registerProductDeleted(String userName, Integer productId) {

    }

    public void registerTagCreated(String userName, Tag tag) {

    }

    public void registerTagUpdated(String userName, Tag oldData, @Valid Tag newData) {

    }

    public void registerTagDeleted(String userName, Integer tagId) {

    }

    public void registerTagSet(String userName, Integer tagId, Integer recipeId) {

    }

    public void registerTagUnset(String userName, Integer tagId, Integer recipeId) {

    }

    public void registerRecipeCreated(String userName, Recipe recipe) {

    }

    public void registerRecipeUpdated(String userName, Recipe oldData, @Valid Recipe newData) {

    }

    public void registerRecipeDeleted(String userName, Integer recipeId) {

    }

    public void registerIngredientCreated(String userName, Ingredient ingredient) {

    }

    public void registerIngredientUpdated(String userName, Ingredient oldData, @Valid Ingredient newData) {

    }

    public void registerIngredientDeleted(String userName, Integer ingredientId) {

    }
}
