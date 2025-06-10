package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.dto.DayDto;
import org.kdepo.solutions.mealplanner.server.dto.MealDto;
import org.kdepo.solutions.mealplanner.server.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.model.Menu;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MenusRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.WeeksRepository;
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
@RequestMapping("/weeks")
public class WeeksController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeksController.class);

    private static final String PK = "week_id";

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private MenusRepository menusRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/{id}")
    public String showWeekDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /weeks/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Week week = weeksRepository.getWeek(id);
        if (week == null) {
            LOGGER.warn("[WEB] Cannot show week details page: week {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canReadWeek(userName, week.getWeekId())) {
            LOGGER.warn("[WEB] Cannot show week details page: user '{}' has no access to week {}", userName, id);
            return "redirect:/menus";
        }

        // Prepare entities
        model.addAttribute("week", week);

        List<Day> daysList = daysRepository.getAllDaysFromWeek(week.getWeekId());
        List<DayDto> days = new ArrayList<>();
        for (Day day : daysList) {
            DayDto dayDto = new DayDto();
            dayDto.setDayId(day.getDayId());
            dayDto.setName(day.getName());

            List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
            List<MealDto> meals = new ArrayList<>();
            for (Meal meal : mealsList) {
                MealDto mealDto = new MealDto();
                mealDto.setMealId(meal.getMealId());
                mealDto.setName(meal.getName());

                List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
                List<RecipeDto> recipes = new ArrayList<>();
                for (Recipe recipe : recipesList) {
                    RecipeDto recipeDto = new RecipeDto();
                    recipeDto.setRecipeId(recipe.getRecipeId());
                    recipeDto.setName(recipe.getName());

                    recipes.add(recipeDto);
                }

                mealDto.setRecipes(recipes);
                meals.add(mealDto);
            }

            dayDto.setMeals(meals);
            days.add(dayDto);
        }
        model.addAttribute("days", days);

        return "week_details";
    }

    @GetMapping("/create")
    public String showWeekCreationForm(Model model, @RequestParam(value = "menu_id") Integer menuId) {
        LOGGER.trace("[WEB] GET /weeks/create?menu_id={}", menuId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show week creation form: anonymous users cannot create weeks");
            return "redirect:/menus";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateWeek(userName)) {
            LOGGER.warn("[WEB] Cannot show week creation form: user '{}' cannot create weeks", userName);
            return "redirect:/menus";
        }

        if (menuId == null) {
            LOGGER.warn("[WEB] Cannot show week creation form: menu is not provided for week creation");
            return "redirect:/menus";
        }

        Menu menu = menusRepository.getMenu(menuId);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show week creation form: menu {} was not found", menuId);
            return "redirect:/menus";
        }
        if (Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            LOGGER.warn("[WEB] Cannot show week creation form: menu {} type mismatch", menuId);
            return "redirect:/menus";
        }
        model.addAttribute("menu", menu);

        Week week = new Week();
        week.setWeekId(-1);
        week.setMenuId(menuId);

        List<Week> weeksList = weeksRepository.getAllWeeksFromMenu(menuId);
        int orderNumber = weeksList.size() + 1;
        week.setName("Неделя " + orderNumber);
        week.setOrderNumber(orderNumber);
        model.addAttribute("week", week);

        return "week_create";
    }

    @PostMapping("/create")
    public String acceptWeekCreationForm(@Valid Week week, BindingResult result) {
        LOGGER.trace("[WEB] POST /weeks/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept week creation form: anonymous users cannot create weeks");
            return "redirect:/menus";
        }

        // Operation availability checks
        if (!controlService.canCreateWeek(userName)) {
            LOGGER.warn("[WEB] Cannot accept week creation form: user '{}' cannot create weeks", userName);
            return "redirect:/menus";
        }

        // Validate that provided data is correct
        String weekName = week.getName();
        if (weekName == null || weekName.isEmpty()) {
            FieldError fieldError = new FieldError("week", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "week_create";
        }

        if (weekName.length() > 20) {
            FieldError fieldError = new FieldError("week", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "week_create";
        }

        Integer menuId = week.getMenuId();

        if (menuId == null) {
            LOGGER.warn("[WEB] Menu is not provided for week creation");
            return "redirect:/menus";
        }

        Menu menu = menusRepository.getMenu(menuId);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot accept week creation form: menu {} was not found", menuId);
            return "redirect:/menus";
        }
        if (Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            LOGGER.warn("[WEB] Cannot accept week creation form: menu {} is not suitable for grouping by weeks", menuId);
            return "redirect:/menus";
        }

        // Generate primary key for new entity
        Integer weekId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        week.setWeekId(weekId);

        // Create entity
        Week createdWeek = weeksRepository.addWeek(
                week.getWeekId(),
                week.getMenuId(),
                week.getName(),
                week.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerWeekCreated(userName, createdWeek);

        // Check and update menu if necessary
        if (Constants.MenuType.UNDEFINED.equals(menu.getMenuTypeId())) {
            Menu oldData = menusRepository.getMenu(menuId);

            menu.setMenuTypeId(Constants.MenuType.DAYS_GROUPED_BY_WEEKS);
            menusRepository.updateMenu(
                    menu.getMenuId(),
                    menu.getMenuTypeId(),
                    menu.getName(),
                    menu.getActive()
            );

            // Register operation in system events log
            logService.registerMenuUpdated(userName, oldData, menu);
        }

        return "redirect:/menus/" + menu.getMenuId();
    }

    @GetMapping("/{id}/update")
    public String showWeekModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /weeks/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show week modification form: anonymous user cannot modify weeks");
            return "redirect:/weeks/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Week week = weeksRepository.getWeek(id);
        if (week == null) {
            LOGGER.warn("[WEB] Cannot show week modification form: week {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyWeek(userName, week.getWeekId())) {
            LOGGER.warn("[WEB] Cannot show week modification form: user '{}' has no access to week {} modification", userName, id);
            return "redirect:/weeks/" + week.getWeekId();
        }

        model.addAttribute("week", week);

        return "week_update";
    }

    @PostMapping("/{id}/update")
    public String acceptWeekModificationForm(@Valid Week week, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /weeks/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept week modification form: anonymous user cannot modify weeks");
            return "redirect:/weeks/" + id;
        }

        Week weekFromDb = weeksRepository.getWeek(week.getWeekId());
        if (weekFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept week modification form: week {} was not found", id);
            return "redirect:/menus";
        }

        if (!week.getWeekId().equals(id)) {
            LOGGER.warn("[WEB] Cannot accept week modification form: week id mismatch: {} and {}", week.getWeekId(), id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyWeek(userName, weekFromDb.getWeekId())) {
            LOGGER.warn("[WEB] Cannot accept week modification form: user '{}' has no access to week {} modification", userName, id);
            return "redirect:/weeks/" + weekFromDb.getWeekId();
        }

        // Validate that provided data is correct
        String weekName = week.getName();
        if (weekName == null || weekName.isEmpty()) {
            FieldError nameFieldError = new FieldError("week", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "week_update";
        }

        if (weekName.length() > 20) {
            FieldError nameFieldError = new FieldError("week", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "week_update";
        }

        // Adjust not editable data
        week.setMenuId(weekFromDb.getMenuId());
        week.setOrderNumber(weekFromDb.getOrderNumber());

        // Update entity
        weeksRepository.updateWeek(
                week.getWeekId(),
                week.getMenuId(),
                week.getName(),
                week.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerWeekUpdated(userName, weekFromDb, week);

        return "redirect:/weeks/" + week.getWeekId();
    }

    @GetMapping("/{id}/delete")
    public String showWeekDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /weeks/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show week deletion form: anonymous user cannot delete weeks");
            return "redirect:/days/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Week week = weeksRepository.getWeek(id);
        if (week == null) {
            LOGGER.warn("[WEB] Cannot show week deletion form: week {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteWeek(userName, week.getWeekId())) {
            LOGGER.warn("[WEB] Cannot show week deletion form: user '{}' has no access to week {} deletion", userName, id);
            return "redirect:/weeks/" + week.getWeekId();
        }

        model.addAttribute("week", week);

        return "week_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptWeekDeletionForm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        LOGGER.trace("[WEB] POST /weeks/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept week deletion form: anonymous user cannot delete weeks");
            return "redirect:/weeks/" + id;
        }

        Week weekFromDb = weeksRepository.getWeek(id);
        if (weekFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept week deletion form: week {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteWeek(userName, weekFromDb.getWeekId())) {
            LOGGER.warn("[WEB] Cannot accept week deletion form: user '{}' has no access to week {} deletion", userName, id);
            return "redirect:/weeks/" + weekFromDb.getWeekId();
        }

        Menu menu = menusRepository.getMenu(weekFromDb.getMenuId());

        // Delete dependent entities
        List<Day> daysList = daysRepository.getAllDaysFromWeek(weekFromDb.getWeekId());
        for (Day day : daysList) {
            List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
            for (Meal meal : mealsList) {
                List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
                for (Recipe recipe : recipesList) {
                    recipesRepository.deleteRecipeFromMeal(recipe.getRecipeId(), meal.getMealId());
                }
                mealsRepository.deleteMeal(meal.getMealId());
                // TODO register operations?
            }
            daysRepository.deleteDay(day.getDayId());
            // TODO register operations?
        }

        // Delete entity
        weeksRepository.deleteWeek(weekFromDb.getWeekId());

        // Register operation in system events log
        logService.registerWeekDeleted(userName, weekFromDb.getWeekId());

        // Update menu type if no more weeks
        if (Constants.MenuType.DAYS_GROUPED_BY_WEEKS.equals(menu.getMenuTypeId())) {
            List<Week> weeksList = weeksRepository.getAllWeeksFromMenu(menu.getMenuId());
            if (weeksList.isEmpty()) {
                Menu oldData = menusRepository.getMenu(weekFromDb.getMenuId());

                menu.setMenuTypeId(Constants.MenuType.UNDEFINED);
                menusRepository.updateMenu(
                        menu.getMenuId(),
                        menu.getMenuTypeId(),
                        menu.getName(),
                        menu.getActive()
                );

                // Register operation in system events log
                logService.registerMenuUpdated(userName, oldData, menu);
            }
        }

        return "redirect:/menus/" + menu.getMenuId();
    }
}
