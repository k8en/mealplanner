package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.IngredientsRepository;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.repository.UnitsRepository;
import org.kdepo.solutions.mealplanner.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.service.OperationsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
    private RecipesRepository recipesRepository;

    @Autowired
    private UnitsRepository unitsRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/{id}")
    public String showIngredientDetailsPage(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            return "redirect:/recipes_list";
        }

        if (!controlService.canReadIngredient(userName, ingredient.getIngredientId())) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot read ingredient info");
            return "redirect:/recipes_list";
        }

        model.addAttribute("ingredient", ingredient);

        return "ingredient_details";
    }

    @GetMapping("/create")
    public String showIngredientCreationForm(Model model, @RequestParam("recipe_id") Integer recipeId) {
        System.out.println("[WEB]" + " GET " + "/ingredients/create?recipe_id=" + recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot create ingredients");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateIngredient(userName)) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot create ingredients");
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + recipeId);
            return "redirect:/recipes";
        }

        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        List<Unit> units = unitsRepository.getAllUnits();
        model.addAttribute("units", units);

        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(-1);
        ingredient.setRecipeId(recipe.getRecipeId());
        ingredient.setAmount(0);
        model.addAttribute("ingredient", ingredient);

        return "ingredient_create";
    }

    @PostMapping("/create")
    public String acceptIngredientCreationForm(@Valid Ingredient ingredient,
                                               @RequestParam("recipe_id") Integer recipeId,
                                               BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/ingredients/create?recipe_id=" + recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot create ingredients");
            return "redirect:/recipes";
        }

        // Operation availability checks
        if (!controlService.canCreateIngredient(userName)) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot create ingredients");
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + recipeId);
            return "redirect:/recipes";
        }

        // Validate that provided data is correct
        String ingredientName = ingredient.getName();
        if (ingredientName == null || ingredientName.isEmpty()) {
            FieldError fieldError = new FieldError("ingredient", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        if (ingredientName.length() > 50) {
            FieldError fieldError = new FieldError("ingredient", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        Product product = productsRepository.getProduct(ingredient.getProductId());
        if (product == null) {
            FieldError fieldError = new FieldError("ingredient", "product", "Продукт не может быть использован!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (!controlService.canReadProduct(userName, product.getProductId())) {
            FieldError fieldError = new FieldError("ingredient", "productId", "Продукт не может быть использован!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        Unit unit = unitsRepository.getUnit(ingredient.getUnitId());
        if (unit == null) {
            FieldError fieldError = new FieldError("ingredient", "unitId", "Единица измерения не может быть использована!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (!controlService.canReadUnit(userName, unit.getUnitId())) {
            FieldError fieldError = new FieldError("ingredient", "unitId", "Единица измерения не может быть использована!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        if (ingredient.getAmount() == null) {
            FieldError fieldError = new FieldError("ingredient", "amount", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (ingredient.getAmount() < 0) {
            FieldError fieldError = new FieldError("ingredient", "amount", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        // Generate primary key for new entity
        Integer ingredientId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        ingredient.setIngredientId(ingredientId);

        // Create entity
        Ingredient ingredientCreated = ingredientsRepository.addIngredient(
                ingredient.getIngredientId(),
                ingredient.getName(),
                ingredient.getRecipeId(),
                ingredient.getProductId(),
                ingredient.getAmount(),
                ingredient.getUnitId()
        );

        // Register operation in system events log
        logService.registerIngredientCreated(userName, ingredientCreated);

        return "redirect:/recipes/" + recipeId;
    }

    @GetMapping("/{id}/update")
    public String showIngredientModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to recipe: anonymous user cannot modify ingredient");
            return "redirect:/recipes/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            System.out.println("Redirect to recipes list: ingredient not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyIngredient(userName, ingredient.getIngredientId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot modify ingredients");
            return "redirect:/recipes/" + ingredient.getRecipeId();
        }

        model.addAttribute("ingredient", ingredient);

        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        List<Unit> units = unitsRepository.getAllUnits();
        model.addAttribute("units", units);

        return "ingredient_update";
    }

    @PostMapping("/{id}/update")
    public String acceptIngredientModificationForm(@Valid Ingredient ingredient, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/ingredients/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to recipes: anonymous user cannot modify ingredients");
            return "redirect:/recipes/" + id;
        }

        // Operation availability checks
        if (!controlService.canCreateIngredient(userName)) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot create ingredients");
            return "redirect:/recipes";
        }

        if (!ingredient.getIngredientId().equals(id)) {
            System.out.println("Redirect to recipes list: ingredient id mismatch " + id + " " + ingredient.getIngredientId());
            return "redirect:/recipes";
        }

        Ingredient ingredientFromDb = ingredientsRepository.getIngredient(id);
        if (ingredientFromDb == null) {
            System.out.println("Redirect to recipes list: ingredient not found " + id);
            return "redirect:/recipes";
        }

        if (!ingredient.getRecipeId().equals(ingredientFromDb.getRecipeId())) {
            System.out.println("Redirect to recipes list: ingredient recipe id mismatch " + ingredientFromDb.getRecipeId() + " " + ingredient.getRecipeId());
            return "redirect:/recipes";
        }

        // Validate that provided data is correct
        String ingredientName = ingredient.getName();
        if (ingredientName == null || ingredientName.isEmpty()) {
            FieldError fieldError = new FieldError("ingredient", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        if (ingredientName.length() > 50) {
            FieldError fieldError = new FieldError("ingredient", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        Product product = productsRepository.getProduct(ingredient.getProductId());
        if (product == null) {
            FieldError fieldError = new FieldError("ingredient", "product", "Продукт не может быть использован!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (!controlService.canReadProduct(userName, product.getProductId())) {
            FieldError fieldError = new FieldError("ingredient", "productId", "Продукт не может быть использован!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        Unit unit = unitsRepository.getUnit(ingredient.getUnitId());
        if (unit == null) {
            FieldError fieldError = new FieldError("ingredient", "unitId", "Единица измерения не может быть использована!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (!controlService.canReadUnit(userName, unit.getUnitId())) {
            FieldError fieldError = new FieldError("ingredient", "unitId", "Единица измерения не может быть использована!");
            result.addError(fieldError);
            return "ingredient_create";
        }

        if (ingredient.getAmount() == null) {
            FieldError fieldError = new FieldError("ingredient", "amount", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "ingredient_create";
        }
        if (ingredient.getAmount() < 0) {
            FieldError fieldError = new FieldError("ingredient", "amount", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "ingredient_create";
        }

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
        logService.registerIngredientUpdated(userName, ingredientFromDb, ingredient);

        return "redirect:/recipes/" + ingredient.getRecipeId();
    }

    @GetMapping("/{id}/delete")
    public String showIngredientDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/ingredients/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to recipes: anonymous user cannot delete ingredients");
            return "redirect:/recipes/";
        }
        model.addAttribute("isLoggedIn", userName != null);

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            System.out.println("Redirect to recipes list: ingredient not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteIngredient(userName, ingredient.getIngredientId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot delete ingredients");
            return "redirect:/recipes/" + ingredient.getRecipeId();
        }

        model.addAttribute("ingredient", ingredient);

        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        List<Unit> units = unitsRepository.getAllUnits();
        model.addAttribute("units", units);

        return "ingredient_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptIngredientDeletionForm(@PathVariable Integer id) {
        System.out.println("[WEB]" + " POST " + "/ingredients/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to recipes: anonymous user cannot delete ingredients");
            return "redirect:/recipes";
        }

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            System.out.println("Redirect to recipes list: ingredient not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteIngredient(userName, ingredient.getIngredientId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot delete ingredients");
            return "redirect:/recipes/" + ingredient.getRecipeId();
        }

        // Delete entity
        ingredientsRepository.deleteIngredient(ingredient.getIngredientId());

        // Register operation in system events log
        logService.registerIngredientDeleted(userName, ingredient.getIngredientId());

        return "redirect:/recipes/" + ingredient.getRecipeId();
    }
}
