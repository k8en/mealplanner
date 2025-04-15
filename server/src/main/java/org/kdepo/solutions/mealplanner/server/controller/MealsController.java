package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/meals")
public class MealsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MealsController.class);

    private static final String PK = "meal_id";

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/{id}")
    public String showMealDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /meals/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Meal meal = mealsRepository.getMeal(id);
        if (meal == null) {
            LOGGER.warn("[WEB] Cannot show meal details page: meal {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canReadMeal(userName, meal.getMealId())) {
            LOGGER.warn("[WEB] Cannot show meal details page: user '{}' has no access to meal {}", userName, id);
            return "redirect:/profiles";
        }

        // Prepare entities
        model.addAttribute("meal", meal);

        List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
        List<RecipeDto> recipes = new ArrayList<>();
        for (Recipe recipe : recipesList) {
            RecipeDto recipeDto = new RecipeDto();
            recipeDto.setRecipeId(recipe.getRecipeId());
            recipeDto.setName(recipe.getName());

            recipes.add(recipeDto);
        }

        model.addAttribute("recipes", recipes);

        return "meal_details";
    }

    @GetMapping("/create")
    public String showMealCreationForm(Model model, @RequestParam(value = "day_id") Integer dayId) {
        LOGGER.trace("[WEB] GET /meals/create?day_id={}", dayId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show meal creation form: anonymous users cannot create meals");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateMeal(userName)) {
            LOGGER.warn("[WEB] Cannot show meal creation form: user '{}' cannot create meals", userName);
            return "redirect:/profiles";
        }

        if (dayId == null) {
            LOGGER.warn("[WEB] Cannot show meal creation form: day is not provided");
            return "redirect:/profiles";
        }

        Day day = daysRepository.getDay(dayId);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show meal creation form: day {} was not found", dayId);
            return "redirect:/profiles";
        }

        // Prepare entity with default values
        List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
        Integer orderNumber = mealsList.size() + 1;

        Meal meal = new Meal();
        meal.setMealId(-1);
        meal.setDayId(dayId);
        //meal.setName();
        meal.setOrderNumber(orderNumber);

        model.addAttribute("meal", meal);

        return "meal_create";
    }

    @PostMapping("/create")
    public String acceptMealCreationForm(@Valid Meal meal, BindingResult result) {
        LOGGER.trace("[WEB] POST /meals/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept meal creation form: anonymous users cannot create meals");
            return "redirect:/profiles";
        }

        // Operation availability checks
        if (!controlService.canCreateMeal(userName)) {
            LOGGER.warn("[WEB] Cannot accept meal creation form: user '{}' cannot create meals", userName);
            return "redirect:/profiles";
        }

        // Validate that provided data is correct
        String mealName = meal.getName();
        if (mealName == null || mealName.isEmpty()) {
            FieldError fieldError = new FieldError("meal", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "meal_create";
        }

        if (mealName.length() > 20) {
            FieldError fieldError = new FieldError("meal", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "meal_create";
        }

        Integer dayId = meal.getDayId();
        if (dayId == null) {
            LOGGER.warn("[WEB] Cannot accept meal creation form: day is not provided");
            return "redirect:/profiles";
        }

        Day day = daysRepository.getDay(dayId);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot accept meal creation form: day {} was not found", dayId);
            return "redirect:/profiles";
        }

        List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
        Integer orderNumber = mealsList.size() + 1;
        meal.setOrderNumber(orderNumber);

        // Generate primary key for new entity
        Integer mealId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        meal.setMealId(mealId);

        // Create entity
        Meal createdMeal = mealsRepository.addMeal(
                meal.getMealId(),
                meal.getDayId(),
                meal.getName(),
                meal.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerMealCreated(userName, createdMeal);

        return "redirect:/meals/" + meal.getMealId();
    }

    @GetMapping("/{id}/update")
    public String showMealModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /meals/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show meal modification form: anonymous user cannot modify meal");
            return "redirect:/meals/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Meal meal = mealsRepository.getMeal(id);
        if (meal == null) {
            LOGGER.warn("[WEB] Cannot show meal modification form: meal {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyMeal(userName, meal.getMealId())) {
            LOGGER.warn("[WEB] Cannot show meal modification form: user '{}' has no access to meal {} modification", userName, id);
            return "redirect:/meals/" + meal.getMealId();
        }

        model.addAttribute("meal", meal);

        return "meal_update";
    }

    @PostMapping("/{id}/update")
    public String acceptMealModificationForm(@Valid Meal meal, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /meals/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept meal modification form: anonymous user cannot modify meals");
            return "redirect:/meals/" + id;
        }

        Meal mealFromDb = mealsRepository.getMeal(meal.getMealId());
        if (mealFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept meal modification form: meal {} was not found", id);
            return "redirect:/profiles";
        }

        if (!meal.getMealId().equals(id)) {
            LOGGER.warn("[WEB] Cannot accept meal modification form: meal id mismatch: {} and {}", meal.getMealId(), id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyMeal(userName, mealFromDb.getMealId())) {
            LOGGER.warn("[WEB] Cannot accept meal modification form: user '{}' has no access to meal {} modification", userName, id);
            return "redirect:/meals/" + mealFromDb.getMealId();
        }

        // Validate that provided data is correct
        String mealName = meal.getName();
        if (mealName == null || mealName.isEmpty()) {
            FieldError nameFieldError = new FieldError("meal", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "meal_update";
        }

        if (mealName.length() > 20) {
            FieldError nameFieldError = new FieldError("meal", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "meal_update";
        }

        Day day = daysRepository.getDay(meal.getDayId());
        if (day == null) {
            LOGGER.warn("[WEB] Cannot accept meal modification form: day {} was not found", meal.getDayId());
            return "redirect:/meals/" + meal.getMealId();
        }

        List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
        Integer orderNumber = mealsList.size() + 1;
        meal.setOrderNumber(orderNumber);

        // Update entity
        mealsRepository.updateMeal(
                meal.getMealId(),
                meal.getDayId(),
                meal.getName(),
                meal.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerMealUpdated(userName, mealFromDb, meal);

        return "redirect:/meals/" + meal.getMealId();
    }

    @GetMapping("/{id}/delete")
    public String showMealDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /meals/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show meal deletion form: anonymous user cannot delete meals");
            return "redirect:/meals/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Meal meal = mealsRepository.getMeal(id);
        if (meal == null) {
            LOGGER.warn("[WEB] Cannot show meal deletion form: meal {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteMeal(userName, meal.getMealId())) {
            LOGGER.warn("[WEB] Cannot show meal deletion form: user '{}' has no access to meal {} deletion", userName, id);
            return "redirect:/meals/" + meal.getMealId();
        }

        model.addAttribute("meal", meal);

        return "meal_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptMealDeletionForm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        LOGGER.trace("[WEB] POST /days/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept meal deletion form: anonymous user cannot delete meals");
            return "redirect:/meals/" + id;
        }

        Meal mealFromDb = mealsRepository.getMeal(id);
        if (mealFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept meal deletion form: meal {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteMeal(userName, mealFromDb.getMealId())) {
            LOGGER.warn("[WEB] Cannot accept meal deletion form: user '{}' has no access to meal {} deletion", userName, id);
            return "redirect:/meals/" + mealFromDb.getMealId();
        }

        // Delete dependent entities
        List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(mealFromDb.getMealId());
        for (Recipe recipe : recipesList) {
            recipesRepository.deleteRecipeFromMeal(recipe.getRecipeId(), mealFromDb.getMealId());
            // TODO register operations?
        }

        // Delete entity
        mealsRepository.deleteMeal(mealFromDb.getMealId());

        // Register operation in system events log
        logService.registerMealDeleted(userName, mealFromDb.getMealId());

        return "redirect:/days/" + mealFromDb.getDayId();
    }
}
