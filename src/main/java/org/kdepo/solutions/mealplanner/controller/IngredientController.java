package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.IngredientsRepository;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.repository.UnitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/ingredients")
public class IngredientController {

    private static final String PK = "ingredient_id";

    @Autowired
    private IngredientsRepository ingredientsRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private UnitsRepository unitsRepository;

    @GetMapping("/{id}")
    public String showIngredientDetailsPage(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = authentication.getName();
            model.addAttribute("userName", userName);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient != null) {
            model.addAttribute("ingredient", ingredient);
            return "ingredient_details";
        } else {
            return "redirect:/recipes_list";
        }
    }

    @GetMapping("/create")
    public String showIngredientCreationForm(Model model, @RequestParam("recipe_id") Integer recipeId) {
        System.out.println("[WEB]" + " GET " + "/ingredients/create?recipe_id=" + recipeId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = authentication.getName();
            model.addAttribute("userName", userName);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        List<Unit> units = unitsRepository.getAllUnits();
        model.addAttribute("units", units);

        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(-1);
        ingredient.setRecipeId(recipeId);
        model.addAttribute("ingredient", ingredient);

        return "ingredient_create";
    }

    @PostMapping("/create")
    public String acceptIngredientCreationForm(@Valid Ingredient ingredient,
                                               @RequestParam("recipe_id") Integer recipeId,
                                               BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/ingredients/create?recipe_id=" + recipeId);

        // Validate that this operation is allowed by the current user
        // TODO

        // TODO add other validations

        // Generate primary key for new entity
        Integer ingredientId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        ingredient.setIngredientId(ingredientId);

        // Create entity
        ingredientsRepository.addIngredient(
                ingredient.getIngredientId(),
                ingredient.getName(),
                ingredient.getRecipeId(),
                ingredient.getProductId(),
                ingredient.getAmount(),
                ingredient.getUnitId()
        );

        // Register operation in system events log
        // TODO

        return "redirect:/recipes/" + recipeId;
    }

    @GetMapping("/{id}/update")
    public String showIngredientModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id + "/update");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = authentication.getName();
            model.addAttribute("userName", userName);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        // Validate that this operation is allowed by the current user
        // TODO

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient != null) {
            model.addAttribute("ingredient", ingredient);

            List<Product> products = productsRepository.getAllProducts();
            model.addAttribute("products", products);

            List<Unit> units = unitsRepository.getAllUnits();
            model.addAttribute("units", units);

            return "ingredient_update";
        } else {
            return "redirect:/ingredients";
        }
    }

    @PostMapping("/{id}/update")
    public String acceptIngredientModificationForm(@Valid Ingredient ingredient, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/ingredients/" + id + "/update");

        // Validate that this operation is allowed by the current user
        // TODO

        // TODO add other validations

        // Update entity
        ingredientsRepository.updateIngredient(
                ingredient.getIngredientId(),
                ingredient.getName(),
                ingredient.getRecipeId(),
                ingredient.getProductId(),
                ingredient.getAmount(),
                ingredient.getUnitId()
        );

        // Register operation in system events log
        // TODO

        return "redirect:/recipes/" + ingredient.getRecipeId();
    }

    @GetMapping("/{id}/delete")
    public String showIngredientDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id + "/delete");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = authentication.getName();
            model.addAttribute("userName", userName);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        // Validate that this operation is allowed by the current user
        // TODO

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient != null) {
            model.addAttribute("ingredient", ingredient);

            List<Product> products = productsRepository.getAllProducts();
            model.addAttribute("products", products);

            List<Unit> units = unitsRepository.getAllUnits();
            model.addAttribute("units", units);

            return "ingredient_delete";
        }

        return "redirect:/recipes";
    }

    @PostMapping("/{id}/delete")
    public String acceptIngredientDeletionForm(@PathVariable Integer id) {
        System.out.println("[WEB]" + " POST " + "/ingredients/" + id + "/delete");

        // Validate that this operation is allowed by the current user
        // TODO

        if (id == null) {
            System.out.println("[WEB] Error! Ingredient id not provided!");
            return "redirect:/recipes";
        }

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            return "redirect:/recipes";
        }

        // Delete entity
        ingredientsRepository.deleteIngredient(ingredient.getIngredientId());

        // Register operation in system events log
        // TODO

        return "redirect:/recipes/" + ingredient.getRecipeId();
    }

}
