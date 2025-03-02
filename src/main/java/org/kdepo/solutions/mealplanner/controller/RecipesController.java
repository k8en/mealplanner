package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.kdepo.solutions.mealplanner.model.TagSelectable;
import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.repository.TagsRepository;
import org.kdepo.solutions.mealplanner.repository.UnitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ProductsRepository productsRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private UnitsRepository unitsRepository;

    @GetMapping
    public String showRecipesListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes");

        List<Recipe> recipes = recipesRepository.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "recipes_list";
    }

    @GetMapping("/{id}")
    public String showRecipeDetailsPage(@PathVariable Integer id,
                                        @RequestParam("portions") Optional<String> portions,
                                        Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + (portions.map(s -> "?portions=" + s).orElse("")));

        if (portions.isPresent()) {
            try {
                defaultPortioning = Integer.parseInt(portions.get());
            } catch (Exception e) {
                System.out.println("Cannot convert to portions value: " + portions.get());
            }
        }
        model.addAttribute("portions", defaultPortioning);

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe != null) {
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
        } else {
            return "redirect:/recipes";
        }
    }

    @GetMapping("/create")
    public String showRecipeCreationForm(Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/create");

        Recipe recipe = new Recipe();
        recipe.setRecipeId(-1);
        model.addAttribute("recipe", recipe);

        return "recipe_create";
    }

    @PostMapping("/create")
    public String acceptRecipeCreationForm(@Valid Recipe recipe, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/recipes/create");

        // Validate that this operation is allowed by the current user
        // TODO

        // Validate that provided data is correct
        String recipeName = recipe.getName();
        if (recipeName == null || recipeName.isEmpty()) {
            FieldError nameFieldError = new FieldError("recipe", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        if (recipeName.length() > 200) {
            FieldError nameFieldError = new FieldError("recipe", "name", "Название не может быть длиннее 200 символов!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        if (recipe.getDescription() != null && recipe.getDescription().length() > 2000) {
            FieldError nameFieldError = new FieldError("recipe", "description", "Описание не может быть длиннее 2000 символов!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        // TODO add other validations

        // Generate primary key for new entity
        Integer recipeId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        recipe.setRecipeId(recipeId);

        // Create entity
        recipesRepository.addRecipe(
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
        // TODO

        return "redirect:/recipes/" + recipe.getRecipeId();
    }

    @GetMapping("/{id}/update")
    public String showRecipeModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/update");

        // Validate that this operation is allowed by the current user
        // TODO

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe != null) {
            model.addAttribute("recipe", recipe);
            return "recipe_update";
        } else {
            return "redirect:/recipes";
        }
    }

    @PostMapping("/{id}/update")
    public String acceptRecipeModificationForm(@Valid Recipe recipe, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/update");

        // Validate that this operation is allowed by the current user
        // TODO

        // Validate that provided data is correct
        String recipeName = recipe.getName();
        if (recipeName == null || recipeName.isEmpty()) {
            FieldError nameFieldError = new FieldError("recipe", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        if (recipeName.length() > 200) {
            FieldError nameFieldError = new FieldError("recipe", "name", "Название не может быть длиннее 200 символов!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        if (recipe.getDescription() != null && recipe.getDescription().length() > 2000) {
            FieldError nameFieldError = new FieldError("recipe", "description", "Описание не может быть длиннее 2000 символов!");
            result.addError(nameFieldError);
            return "recipe_create";
        }

        // TODO add other validations

        // Validate that object is exist
        boolean isExist = recipesRepository.getRecipe(id) != null;
        if (!isExist) {
            return "redirect:/recipes";
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
        // TODO

        return "redirect:/recipes/" + recipe.getRecipeId();
    }

    @GetMapping("/{id}/delete")
    public String showRecipeDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + "/delete");

        // Validate that this operation is allowed by the current user
        // TODO

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            return "redirect:/recipes";
        }
        model.addAttribute("recipe", recipe);

        return "recipe_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptRecipeDeletionForm(@PathVariable Integer id) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/delete");

        // Validate that this operation is allowed by the current user
        // TODO

        if (id == null) {
            System.out.println("[WEB] Error! Recipe id not provided!");
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            return "redirect:/recipes";
        }

        // Delete ingredients
        // TODO

        // Delete tag bindings
        List<Tag> tagsByRecipe = tagsRepository.getAllTagsForRecipe(recipe.getRecipeId());
        for (Tag tag : tagsByRecipe) {
            tagsRepository.deleteTagFromRecipe(tag.getTagId(), recipe.getRecipeId());
        }

        // Delete entity
        recipesRepository.deleteRecipe(recipe.getRecipeId());

        // Register operation in system events log
        // TODO

        return "redirect:/recipes";
    }

    @GetMapping("/{id}/tags")
    public String showRecipeTagsForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + id + "/tags");

        // Validate that this operation is allowed by the current user
        // TODO

        Recipe recipe = recipesRepository.getRecipe(id);
        if (recipe == null) {
            return "redirect:/recipes";
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
    public String acceptRecipeTagsForm(@PathVariable Integer id, @RequestParam("selectedTags") ArrayList<Integer> selectedTags) {
        System.out.println("[WEB]" + " POST " + "/recipes/" + id + "/tags");

        List<Tag> selectedTagsFromDb = tagsRepository.getAllTagsForRecipe(id);
        List<Integer> selectedTagsIdsFromDb = selectedTagsFromDb.stream()
                .map(Tag::getTagId)
                .toList();
        List<Integer> tagsIdsToDelete = selectedTagsIdsFromDb.stream()
                .filter(e -> !selectedTags.contains(e))
                .toList();
        List<Integer> tagsIdsToAdd = selectedTags.stream()
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
