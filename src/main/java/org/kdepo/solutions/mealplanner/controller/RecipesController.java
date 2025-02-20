package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.kdepo.solutions.mealplanner.model.Unit;
import org.kdepo.solutions.mealplanner.repository.MealPlannerRecipesRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerUnitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private static final DecimalFormat PORTIONING_FORMAT = new DecimalFormat("#.#");
    private static final String RECIPE_NAME_TEMPLATE = "{0} (на {1} {2}), расчетная порционность - {3}";

    private int defaultPortioning = 2;

    @Autowired
    private MealPlannerRecipesRepository recipesRepository;

    @Autowired
    private MealPlannerUnitsRepository unitsRepository;

    @GetMapping
    public String showRecipesListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes");

        List<Recipe> recipes = recipesRepository.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "recipes_list";
    }

    @GetMapping("/{rid}")
    public String showDeviceDetailsPage(@PathVariable Integer rid,
                                        @RequestParam("portions") Optional<String> portions,
                                        Model model) {
        System.out.println("[WEB]" + " GET " + "/recipes/" + rid
                + (portions.map(s -> "?portions=" + s).orElse(""))
        );

        if (portions.isPresent()) {
            try {
                defaultPortioning = Integer.parseInt(portions.get());
            } catch (Exception e) {
                System.out.println("Cannot convert to portions value: " + portions.get());
            }
        }
        model.addAttribute("portions", defaultPortioning);

        Recipe recipe = recipesRepository.getRecipe(rid);
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
            List<String> ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.getIngredientsList()) {
                Unit unit = unitsRepository.getUnit(ingredient.getUnitId());
                String recalculatedIngredient = ingredient.getName()
                        + " - "
                        + PORTIONING_FORMAT.format(ingredient.getAmount() * portionsMultiplier)
                        + " "
                        + unit.getShortName();

                ingredients.add(recalculatedIngredient);
            }
            recipeDto.setIngredients(ingredients);

            model.addAttribute("recipe", recipeDto);

            List<Tag> tags = recipe.getTagsList();
            model.addAttribute("tags", tags);

            return "recipe_details";
        } else {
            return "redirect:/recipes";
        }
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
