package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.model.Product;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Unit;
import org.kdepo.solutions.mealplanner.shared.repository.IngredientsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.UnitsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(IngredientController.class);

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
        LOGGER.trace("[WEB] GET /ingredients/{}", id);

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
            LOGGER.warn("[WEB] Cannot show ingredient details page: ingredient {} was not found", id);
            return "redirect:/recipes_list";
        }

        if (!controlService.canReadIngredient(userName, ingredient.getIngredientId())) {
            LOGGER.warn("[WEB] Cannot show ingredient details page: user '{}' has no access to ingredient {}", userName, id);
            return "redirect:/recipes_list";
        }

        model.addAttribute("ingredient", ingredient);

        Product product = productsRepository.getProduct(ingredient.getProductId());
        model.addAttribute("product", product);

        Unit unit = unitsRepository.getUnit(ingredient.getUnitId());
        model.addAttribute("unit", unit);

        return "ingredient_details";
    }

    @GetMapping("/create")
    public String showIngredientCreationForm(Model model, @RequestParam("recipe_id") Integer recipeId) {
        LOGGER.trace("[WEB] GET /ingredients/create?recipe_id={}", recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show ingredient creation form: anonymous users cannot create ingredients");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateIngredient(userName)) {
            LOGGER.warn("[WEB] Cannot show ingredient creation form: user '{}' cannot create ingredients", userName);
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            LOGGER.warn("[WEB] Cannot show ingredient creation form: recipe {} was not found", recipeId);
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
        LOGGER.trace("[WEB] POST /ingredients/create?recipe_id={}", recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept ingredient creation form: anonymous users cannot create ingredients");
            return "redirect:/recipes";
        }

        // Operation availability checks
        if (!controlService.canCreateIngredient(userName)) {
            LOGGER.warn("[WEB] Cannot accept ingredient creation form: user '{}' cannot create ingredients", userName);
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            LOGGER.warn("[WEB] Cannot accept ingredient creation form: recipe {} was not found", recipeId);
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
        LOGGER.trace("[WEB] GET /ingredients/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show ingredient modification form: anonymous users cannot modify ingredients");
            return "redirect:/recipes/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            LOGGER.warn("[WEB] Cannot show ingredient modification form: ingredient {} was not found", id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyIngredient(userName, ingredient.getIngredientId())) {
            LOGGER.warn("[WEB] Cannot show ingredient modification form: user '{}' has no access to ingredient {} modification", userName, id);
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
        LOGGER.trace("[WEB] POST /ingredients/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept ingredient modification form: anonymous users cannot modify ingredients");
            return "redirect:/recipes/" + id;
        }

        Ingredient ingredientFromDb = ingredientsRepository.getIngredient(id);
        if (ingredientFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept ingredient modification form: ingredient {} was not found", id);
            return "redirect:/recipes";
        }

        if (!ingredient.getIngredientId().equals(id)) {
            LOGGER.warn("[WEB] Cannot accept ingredient modification form: ingredient id mismatch: {} and {}", ingredient.getIngredientId(), id);
            return "redirect:/recipes";
        }

        if (!ingredient.getRecipeId().equals(ingredientFromDb.getRecipeId())) {
            LOGGER.warn("[WEB] Cannot accept ingredient modification form: recipe id mismatch: {} and {}", ingredientFromDb.getRecipeId(), ingredient.getRecipeId());
            return "redirect:/recipes";
        }

        // Operation availability checks
        if (!controlService.canModifyIngredient(userName, ingredientFromDb.getIngredientId())) {
            LOGGER.warn("[WEB] Cannot accept ingredient modification form: user '{}' has no access to ingredient {} modification", userName, id);
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
        LOGGER.trace("[WEB] GET /ingredients/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show ingredient deletion form: anonymous users cannot delete ingredients");
            return "redirect:/recipes/";
        }
        model.addAttribute("isLoggedIn", userName != null);

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            LOGGER.warn("[WEB] Cannot show ingredient deletion form: ingredient {} was not found", id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteIngredient(userName, ingredient.getIngredientId())) {
            LOGGER.warn("[WEB] Cannot show ingredient deletion form: user '{}' has no access to ingredient {} deletion", userName, id);
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
        LOGGER.trace("[WEB] POST /ingredients/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept ingredient deletion form: anonymous users cannot delete ingredients");
            return "redirect:/recipes";
        }

        Ingredient ingredient = ingredientsRepository.getIngredient(id);
        if (ingredient == null) {
            LOGGER.warn("[WEB] Cannot accept ingredient deletion form: ingredient {} was not found", id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteIngredient(userName, ingredient.getIngredientId())) {
            LOGGER.warn("[WEB] Cannot accept ingredient deletion form: user '{}' has no access to ingredient {} deletion", userName, id);
            return "redirect:/recipes/" + ingredient.getRecipeId();
        }

        // Delete entity
        ingredientsRepository.deleteIngredient(ingredient.getIngredientId());

        // Register operation in system events log
        logService.registerIngredientDeleted(userName, ingredient.getIngredientId());

        return "redirect:/recipes/" + ingredient.getRecipeId();
    }
}
