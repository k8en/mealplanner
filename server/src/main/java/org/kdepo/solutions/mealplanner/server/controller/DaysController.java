package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
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
@RequestMapping("/days")
public class DaysController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaysController.class);

    private static final String PK = "day_id";

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
    public String showDayDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day details page: day {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canReadDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day details page: user '{}' has no access to day {}", userName, id);
            return "redirect:/menus";
        }

        // Prepare entities
        model.addAttribute("day", day);

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
        model.addAttribute("meals", meals);

        return "day_details";
    }

    @GetMapping("/create")
    public String showDayCreationForm(Model model,
                                      @RequestParam(value = "menu_id") Integer menuId,
                                      @RequestParam(value = "week_id", required = false) Integer weekId) {
        LOGGER.trace("[WEB] GET /days/create?menu_id={}&week_id={}", menuId, weekId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day creation form: anonymous users cannot create days");
            return "redirect:/menus";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot show day creation form: user '{}' cannot create days", userName);
            return "redirect:/menus";
        }

        if (menuId == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: menu is not provided for day creation");
            return "redirect:/menus";
        }

        Menu menu = menusRepository.getMenu(menuId);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: menu {} was not found", menuId);
            return "redirect:/menus";
        }

        Week week = null;
        if (weekId != null) {
            week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot show day creation form: week {} was not found", weekId);
                return "redirect:/menus";
            }
        }

        if (week != null && Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: menu {} type mismatch - week is provided, but this this is DAYS_WITHOUT_GROUPING", menuId);
            return "redirect:/menus";
        }

        if (week == null && Constants.MenuType.DAYS_GROUPED_BY_WEEKS.equals(menu.getMenuTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: menu {} type mismatch - week is not provided, but this is DAYS_GROUPED_BY_WEEKS", menuId);
            return "redirect:/menus";
        }

        model.addAttribute("menu", menu);
        model.addAttribute("week", week);

        String name;
        Integer orderNumber;
        if (week == null) {
            List<Day> daysList = daysRepository.getAllDaysFromMenu(menuId);
            orderNumber = daysList.size() + 1;
            name = "День " + orderNumber;
        } else {
            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            orderNumber = daysList.size() + 1;
            if (orderNumber >= 8) {
                LOGGER.warn("[WEB] Cannot show day creation form: all days for week {} are created already", weekId);
                return "redirect:/weeks/" + weekId;
            }

            if (orderNumber == 1) {
                name = "Понедельник";
            } else if (orderNumber == 2) {
                name = "Вторник";
            } else if (orderNumber == 3) {
                name = "Среда";
            } else if (orderNumber == 4) {
                name = "Четверг";
            } else if (orderNumber == 5) {
                name = "Пятница";
            } else if (orderNumber == 6) {
                name = "Суббота";
            } else {
                name = "Воскресенье";
            }
        }

        // Prepare entity with default values
        Day day = new Day();
        day.setDayId(-1);
        day.setMenuId(menuId);
        day.setWeekId(weekId);
        day.setName(name);
        day.setOrderNumber(orderNumber);

        model.addAttribute("day", day);

        return "day_create";
    }

    @PostMapping("/create")
    public String acceptDayCreationForm(@Valid Day day, BindingResult result) {
        LOGGER.trace("[WEB] POST /days/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day creation form: anonymous users cannot create days");
            return "redirect:/menus";
        }

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' cannot create days", userName);
            return "redirect:/menus";
        }

        // Validate that provided data is correct
        String dayName = day.getName();
        if (dayName == null || dayName.isEmpty()) {
            FieldError fieldError = new FieldError("day", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "day_create";
        }

        if (dayName.length() > 20) {
            FieldError fieldError = new FieldError("day", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "day_create";
        }

        Integer menuId = day.getMenuId();
        Integer weekId = day.getWeekId();

        if (menuId == null) {
            LOGGER.warn("[WEB] Menu is not provided for day creation");
            return "redirect:/menus";
        }

        Menu menu = menusRepository.getMenu(menuId);
        if (menu == null) {
            LOGGER.warn("[WEB] Menu {} was not found", menuId);
            return "redirect:/menus";
        }

        if (!controlService.canReadMenu(userName, menuId)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' has no access to menu {}", userName, menuId);
            return "redirect:/menus";
        }

        int menuTypeId;
        if (weekId != null) {
            // Split by weeks
            Week week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot accept day creation form: week {} was not found", weekId);
                return "redirect:/menus/" + menu.getMenuId();
            }
            if (!week.getMenuId().equals(menu.getMenuId())) {
                LOGGER.warn("[WEB] Cannot accept day creation form: menu {} mismatch with menu {} from week {}", menuId, week.getMenuId(), weekId);
                return "redirect:/menus/" + menu.getMenuId();
            }
            day.setWeekId(weekId);

            menuTypeId = Constants.MenuType.DAYS_GROUPED_BY_WEEKS;

            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            int orderNumber = daysList.size() + 1;

            if (orderNumber >= 8) {
                FieldError fieldError = new FieldError("day", "name", "В неделе не может быть больше 7 дней!");
                result.addError(fieldError);
                return "day_create";
            }

            day.setOrderNumber(orderNumber);

        } else {
            // Split by days
            menuTypeId = Constants.MenuType.DAYS_WITHOUT_GROUPING;

            List<Day> daysList = daysRepository.getAllDaysFromMenu(menuId);
            int orderNumber = daysList.size() + 1;
            day.setOrderNumber(orderNumber);

        }

        // Generate primary key for new entity
        Integer dayId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        day.setDayId(dayId);

        // Create entity
        Day createdDay = daysRepository.addDay(
                day.getDayId(),
                day.getMenuId(),
                day.getWeekId(),
                day.getName(),
                day.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerDayCreated(userName, createdDay);

        // Check and update menu if necessary
        if (Constants.MenuType.UNDEFINED.equals(menu.getMenuTypeId())) {
            Menu oldData = menusRepository.getMenu(menuId);

            menu.setMenuTypeId(menuTypeId);
            menusRepository.updateMenu(
                    menu.getMenuId(),
                    menu.getMenuTypeId(),
                    menu.getName(),
                    menu.getActive()
            );

            // Register operation in system events log
            logService.registerMenuUpdated(userName, oldData, menu);
        }

        if (weekId != null) {
            return "redirect:/weeks/" + weekId;
        } else {
            return "redirect:/menus/" + menu.getMenuId();
        }
    }

    @GetMapping("/{id}/update")
    public String showDayModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day modification form: anonymous user cannot modify days");
            return "redirect:/days/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day modification form: day {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day modification form: user '{}' has no access to day {} modification", userName, id);
            return "redirect:/days/" + day.getDayId();
        }

        model.addAttribute("day", day);

        return "day_update";
    }

    @PostMapping("/{id}/update")
    public String acceptDayModificationForm(@Valid Day day, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /days/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day modification form: anonymous user cannot modify days");
            return "redirect:/days/" + id;
        }

        Day dayFromDb = daysRepository.getDay(day.getDayId());
        if (dayFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept day modification form: day {} was not found", id);
            return "redirect:/menus";
        }

        if (!day.getDayId().equals(id)) {
            LOGGER.warn("[WEB] Cannot accept day modification form: day id mismatch: {} and {}", day.getDayId(), id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyDay(userName, dayFromDb.getDayId())) {
            LOGGER.warn("[WEB] Cannot accept day modification form: user '{}' has no access to day {} modification", userName, id);
            return "redirect:/days/" + dayFromDb.getDayId();
        }

        // Validate that provided data is correct
        String dayName = day.getName();
        if (dayName == null || dayName.isEmpty()) {
            FieldError nameFieldError = new FieldError("day", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "day_update";
        }

        if (dayName.length() > 20) {
            FieldError nameFieldError = new FieldError("day", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "day_update";
        }

        // Adjust not editable data
        day.setMenuId(dayFromDb.getMenuId());
        day.setWeekId(dayFromDb.getWeekId());
        day.setOrderNumber(dayFromDb.getOrderNumber());

        // Update entity
        daysRepository.updateDay(
                day.getDayId(),
                day.getMenuId(),
                day.getWeekId(),
                day.getName(),
                day.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerDayUpdated(userName, dayFromDb, day);

        return "redirect:/days/" + day.getDayId();
    }

    @GetMapping("/{id}/delete")
    public String showDayDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day deletion form: anonymous user cannot delete days");
            return "redirect:/days/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day deletion form: day {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day deletion form: user '{}' has no access to day {} deletion", userName, id);
            return "redirect:/days/" + day.getDayId();
        }

        model.addAttribute("day", day);

        return "day_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptDayDeletionForm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        LOGGER.trace("[WEB] POST /days/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day deletion form: anonymous user cannot delete days");
            return "redirect:/days/" + id;
        }

        Day dayFromDb = daysRepository.getDay(id);
        if (dayFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept day deletion form: day {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteDay(userName, dayFromDb.getDayId())) {
            LOGGER.warn("[WEB] Cannot accept day deletion form: user '{}' has no access to day {} deletion", userName, id);
            return "redirect:/days/" + dayFromDb.getDayId();
        }

        Menu menu = menusRepository.getMenu(dayFromDb.getMenuId());

        // Delete dependent entities
        List<Meal> mealsList = mealsRepository.getAllMealsFromDay(dayFromDb.getDayId());
        for (Meal meal : mealsList) {
            List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
            for (Recipe recipe : recipesList) {
                recipesRepository.deleteRecipeFromMeal(recipe.getRecipeId(), meal.getMealId());
            }
            mealsRepository.deleteMeal(meal.getMealId());
            // TODO register operations?
        }

        // Delete entity
        daysRepository.deleteDay(dayFromDb.getDayId());

        // Register operation in system events log
        logService.registerDayDeleted(userName, dayFromDb.getDayId());

        // Update menu type if no more days
        if (Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            List<Day> daysList = daysRepository.getAllDaysFromMenu(menu.getMenuId());
            if (daysList.isEmpty()) {
                Menu oldData = menusRepository.getMenu(dayFromDb.getMenuId());

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

        // Redirect depends on menu type
        if (Constants.MenuType.UNDEFINED.equals(menu.getMenuTypeId())
                || Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            return "redirect:/menus/" + menu.getMenuId();
        } else {
            return "redirect:/weeks/" + dayFromDb.getWeekId();
        }
    }
}
