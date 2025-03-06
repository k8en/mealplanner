package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.kdepo.solutions.mealplanner.model.TagSelectable;
import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.IngredientsRepository;
import org.kdepo.solutions.mealplanner.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.repository.TagsRepository;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recipes")
public class RecipesController {

    private static final String PK = "recipe_id";

    private static final DecimalFormat PORTIONING_FORMAT = new DecimalFormat("#.#");
    private static final String RECIPE_NAME_TEMPLATE = "{0} (на {1} {2}), расчетная порционность - {3}";

    private int defaultPortioning = 2;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private IngredientsRepository ingredientsRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private UnitsRepository unitsRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping
    public String showRecipesListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Prepare entities
        List<Recipe> recipes = recipesRepository.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "recipes_list";
    }

    @GetMapping("/{id}")
    public String showRecipeDetailsPage(@PathVariable Integer id,
                                        @RequestParam("portions") Optional<String> portions,
                                        Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + (portions.map(s -> "?portions=" + s).orElse("")));

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canReadRecipe(userName, recipe.getRecipeId())) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot read recipe info");
            return "redirect:/recipes";
        }

        if (portions.isPresent()) {
            try {
                defaultPortioning = Integer.parseInt(portions.get());
            } catch (Exception e) {
                System.out.println("Cannot convert to portions value: " + portions.get());
            }
        }
        model.addAttribute("portions", defaultPortioning);

        float portionsMultiplier = (defaultPortioning * 1.0f) / recipe.getPortions();

        RecipeDto recipeDto = new RecipeDto();

        recipeDto.setRecipeId(recipe.getRecipeId());

        if (recipe.getName() != null) {
            String portioningWord = getPortioningWord(recipe.getPortions());
            recipeDto.setName(
                    MessageFormat.format(
                            RECIPE_NAME_TEMPLATE,
                            recipe.getName(),
                            recipe.getPortions(),
                            portioningWord,
                            defaultPortioning
                    )
            );
        }

        recipeDto.setSource(recipe.getSource());
        recipeDto.setWeight(recipe.getWeight());
        recipeDto.setCalories(recipe.getCalories());
        recipeDto.setProteins(recipe.getProteins());
        recipeDto.setFats(recipe.getFats());
        recipeDto.setCarbs(recipe.getCarbs());

        // Prepare instruction as paragraphs
        List<String> paragraphs = new ArrayList<>();
        if (recipe.getDescription() != null) {
            String[] paragraphsArray = recipe.getDescription().split("\n");
            paragraphs.addAll(Arrays.asList(paragraphsArray));
            recipeDto.setParagraphs(paragraphs);
        }

        // Prepare ingredients list with recalculation data
        List<Ingredient> ingredients = recipe.getIngredientsList();
        for (Ingredient ingredient : ingredients) {
            Unit unit = unitsRepository.getUnit(ingredient.getUnitId());
            String recalculatedIngredient = ingredient.getName()
                    + " - "
                    + PORTIONING_FORMAT.format(ingredient.getAmount() * portionsMultiplier)
                    + " "
                    + unit.getShortName();

            ingredient.setName(recalculatedIngredient);
        }

        model.addAttribute("recipe", recipeDto);

        model.addAttribute("ingredients", ingredients);

        List<Tag> tags = recipe.getTagsList();
        model.addAttribute("tags", tags);

        return "recipe_details";
    }

    @GetMapping("/create")
    public String showRecipeCreationForm(Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot create recipes");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateRecipe(userName)) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot create recipes");
            return "redirect:/recipes";
        }

        // Prepare entity with default values
        Recipe recipe = new Recipe();
        recipe.setRecipeId(-1);
        recipe.setPortions(defaultPortioning); // TODO exclude into settings
        recipe.setWeight(BigDecimal.ZERO);
        recipe.setCalories(BigDecimal.ZERO);
        recipe.setProteins(BigDecimal.ZERO);
        recipe.setFats(BigDecimal.ZERO);
        recipe.setCarbs(BigDecimal.ZERO);
        model.addAttribute("recipe", recipe);

        return "recipe_create";
    }

    @PostMapping("/create")
    public String acceptRecipeCreationForm(@Valid Recipe recipe, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/recipes/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot create recipes");
            return "redirect:/recipes";
        }

        // Operation availability checks
        if (!controlService.canCreateRecipe(userName)) {
            System.out.println("Redirect to recipes list: user '" + userName + "' cannot create recipes");
            return "redirect:/recipes";
        }

        // Validate that provided data is correct
        String recipeName = recipe.getName();
        if (recipeName == null || recipeName.isEmpty()) {
            FieldError fieldError = new FieldError("recipe", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (recipeName.length() > 200) {
            FieldError fieldError = new FieldError("recipe", "name", "Название не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "recipe_create";
        }

        String recipeDescription = recipe.getDescription();
        if (recipeDescription == null || recipeDescription.isEmpty()) {
            FieldError fieldError = new FieldError("recipe", "description", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (recipeDescription.length() > 2000) {
            FieldError fieldError = new FieldError("recipe", "description", "Описание не может быть длиннее 2000 символов!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (recipe.getSource() != null && recipe.getSource().length() > 200) {
            FieldError fieldError = new FieldError("recipe", "source", "Источник не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "recipe_create";
        }

        Integer recipePortions = recipe.getPortions();
        if (recipePortions == null) {
            FieldError fieldError = new FieldError("recipe", "portions", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (recipePortions <= 0) {
            FieldError fieldError = new FieldError("recipe", "portions", "Количество порций должно быть больше 0!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (recipePortions > 12) {
            FieldError fieldError = new FieldError("recipe", "portions", "Количество порций должно быть меньше 12!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getWeight()) > 0) {
            FieldError fieldError = new FieldError("recipe", "weight", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getCalories()) > 0) {
            FieldError fieldError = new FieldError("recipe", "calories", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getProteins()) > 0) {
            FieldError fieldError = new FieldError("recipe", "proteins", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getFats()) > 0) {
            FieldError fieldError = new FieldError("recipe", "fats", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_create";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getCarbs()) > 0) {
            FieldError fieldError = new FieldError("recipe", "carbs", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_create";
        }

        // Generate primary key for new entity
        Integer recipeId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        recipe.setRecipeId(recipeId);

        // Create entity
        Recipe createdRecipe = recipesRepository.addRecipe(
                recipe.getRecipeId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getSource(),
                recipe.getPortions(),
                recipe.getWeight(),
                recipe.getCalories(),
                recipe.getProteins(),
                recipe.getFats(),
                recipe.getCarbs()
        );

        // Register operation in system events log
        logService.registerRecipeCreated(userName, createdRecipe);

        return "redirect:/recipes/" + recipe.getRecipeId();
    }

    @GetMapping("/{id}/update")
    public String showRecipeModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to recipe: anonymous user cannot modify recipes");
            return "redirect:/recipes/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyRecipe(userName, recipe.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot modify recipes");
            return "redirect:/recipes/" + recipe.getRecipeId();
        }

        model.addAttribute("recipe", recipe);

        return "recipe_update";
    }

    @PostMapping("/{id}/update")
    public String acceptRecipeModificationForm(@Valid Recipe recipe, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to recipe: anonymous user cannot modify recipes");
            return "redirect:/recipes/" + id;
        }

        Recipe recipeFromDb = recipesRepository.getRecipe(id);
        if (recipeFromDb == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyRecipe(userName, recipeFromDb.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot modify recipes");
            return "redirect:/recipes/" + recipeFromDb.getRecipeId();
        }

        // Validate that provided data is correct
        String recipeName = recipe.getName();
        if (recipeName == null || recipeName.isEmpty()) {
            FieldError fieldError = new FieldError("recipe", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (recipeName.length() > 200) {
            FieldError fieldError = new FieldError("recipe", "name", "Название не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "recipe_update";
        }

        String recipeDescription = recipe.getDescription();
        if (recipeDescription == null || recipeDescription.isEmpty()) {
            FieldError fieldError = new FieldError("recipe", "description", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (recipeDescription.length() > 2000) {
            FieldError fieldError = new FieldError("recipe", "description", "Описание не может быть длиннее 2000 символов!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (recipe.getSource() != null && recipe.getSource().length() > 200) {
            FieldError fieldError = new FieldError("recipe", "source", "Источник не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "recipe_update";
        }

        Integer recipePortions = recipe.getPortions();
        if (recipePortions == null) {
            FieldError fieldError = new FieldError("recipe", "portions", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (recipePortions <= 0) {
            FieldError fieldError = new FieldError("recipe", "portions", "Количество порций должно быть больше 0!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (recipePortions > 12) {
            FieldError fieldError = new FieldError("recipe", "portions", "Количество порций должно быть меньше 12!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getWeight()) > 0) {
            FieldError fieldError = new FieldError("recipe", "weight", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getCalories()) > 0) {
            FieldError fieldError = new FieldError("recipe", "calories", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getProteins()) > 0) {
            FieldError fieldError = new FieldError("recipe", "proteins", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getFats()) > 0) {
            FieldError fieldError = new FieldError("recipe", "fats", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_update";
        }

        if (BigDecimal.ZERO.compareTo(recipe.getCarbs()) > 0) {
            FieldError fieldError = new FieldError("recipe", "carbs", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "recipe_update";
        }

        // Update entity
        recipesRepository.updateRecipe(
                recipe.getRecipeId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getSource(),
                recipe.getPortions(),
                recipe.getWeight(),
                recipe.getCalories(),
                recipe.getProteins(),
                recipe.getFats(),
                recipe.getCarbs()
        );

        // Register operation in system events log
        logService.registerRecipeUpdated(userName, recipeFromDb, recipe);

        return "redirect:/recipes/" + recipe.getRecipeId();
    }

    @GetMapping("/{id}/delete")
    public String showRecipeDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to recipe: anonymous user cannot delete recipes");
            return "redirect:/recipes/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteRecipe(userName, recipe.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot delete recipes");
            return "redirect:/recipes/" + recipe.getRecipeId();
        }

        model.addAttribute("recipe", recipe);

        return "recipe_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptRecipeDeletionForm(@PathVariable Integer id) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to recipe: anonymous user cannot delete recipes");
            return "redirect:/recipes/" + id;
        }

        Recipe recipeFromDb = recipesRepository.getRecipe(id);
        if (recipeFromDb == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canDeleteRecipe(userName, recipeFromDb.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot delete recipes");
            return "redirect:/recipes/" + recipeFromDb.getRecipeId();
        }

        // Delete recipe from meal
        List<Integer> allMealsWithRecipe = mealsRepository.getAllMealsWithRecipe(recipeFromDb.getRecipeId());
        for (Integer mealId : allMealsWithRecipe) {
            recipesRepository.deleteRecipeFromMeal(recipeFromDb.getRecipeId(), mealId);
        }

        // Delete tag bindings
        List<Tag> tagsByRecipe = tagsRepository.getAllTagsForRecipe(recipeFromDb.getRecipeId());
        for (Tag tag : tagsByRecipe) {
            tagsRepository.deleteTagFromRecipe(tag.getTagId(), recipeFromDb.getRecipeId());
        }

        // Delete recipe ingredients
        List<Ingredient> allIngredientsFromRecipe = ingredientsRepository.getAllIngredientsFromRecipe(recipeFromDb.getRecipeId());
        for (Ingredient ingredient : allIngredientsFromRecipe) {
            ingredientsRepository.deleteIngredient(ingredient.getIngredientId());

            // Register operation in system events log
            logService.registerIngredientDeleted(userName, ingredient.getIngredientId());
        }

        // Delete entity
        recipesRepository.deleteRecipe(recipeFromDb.getRecipeId());

        // Register operation in system events log
        logService.registerRecipeDeleted(userName, recipeFromDb.getRecipeId());

        return "redirect:/recipes";
    }

    @GetMapping("/{id}/tags")
    public String showRecipeTagsForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + "/tags");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot modify recipes");
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyRecipe(userName, recipe.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot modify recipes");
            return "redirect:/recipes/" + recipe.getRecipeId();
        }

        model.addAttribute("recipe", recipe);

        // Calculate tags selectable
        List<Tag> selectedTags = tagsRepository.getAllTagsForRecipe(recipe.getRecipeId());
        List<Integer> selectedTagsIds = selectedTags.stream()
                .map(Tag::getTagId)
                .toList();
        List<Tag> allTags = tagsRepository.getAllTags();
        List<TagSelectable> tags = new ArrayList<>();
        for (Tag tag : allTags) {
            TagSelectable tagSelectable = new TagSelectable();
            tagSelectable.setTagId(tag.getTagId());
            tagSelectable.setName(tag.getName());
            tagSelectable.setSelected(selectedTagsIds.contains(tag.getTagId()));
            tags.add(tagSelectable);
        }
        model.addAttribute("tags", tags);

        return "recipe_tags";
    }

    @PostMapping("/{id}/tags")
    public String acceptRecipeTagsForm(@PathVariable Integer id,
                                       @RequestParam(value = "selectedTags", required = false) Optional<List<Integer>> selectedTags) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/tags");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect to recipes list: anonymous user cannot modify recipes");
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            System.out.println("Redirect to recipes list: recipe not found " + id);
            return "redirect:/recipes";
        }

        if (!controlService.canModifyRecipe(userName, recipe.getRecipeId())) {
            System.out.println("Redirect back to recipe: user '" + userName + "' cannot modify recipes");
            return "redirect:/recipes/" + recipe.getRecipeId();
        }

        List<Integer> tags = selectedTags.orElse(Collections.emptyList());
        List<Tag> selectedTagsFromDb = tagsRepository.getAllTagsForRecipe(id);
        List<Integer> selectedTagsIdsFromDb = selectedTagsFromDb.stream()
                .map(Tag::getTagId)
                .toList();
        List<Integer> tagsIdsToDelete = selectedTagsIdsFromDb.stream()
                .filter(e -> !tags.contains(e))
                .toList();
        List<Integer> tagsIdsToAdd = tags.stream()
                .filter(e -> !selectedTagsIdsFromDb.contains(e))
                .toList();

        for (Integer tagIdToDelete : tagsIdsToDelete) {
            tagsRepository.deleteTagFromRecipe(tagIdToDelete, id);
        }
        for (Integer tagIdToAdd : tagsIdsToAdd) {
            tagsRepository.addTagToRecipe(tagIdToAdd, id);
        }

        return "redirect:/recipes/" + id;
    }

    private String getPortioningWord(int portions) {
        String portioningWord = "???";
        if (1 == portions) {
            portioningWord = "порцию";
        } else if (2 == portions
                || 3 == portions
                || 4 == portions) {
            portioningWord = "порции";
        } else if (5 == portions
                || 6 == portions
                || 7 == portions
                || 8 == portions
                || 9 == portions
                || 10 == portions) {
            portioningWord = "порций";
        }
        return portioningWord;
    }

}
