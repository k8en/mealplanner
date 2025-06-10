package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.dto.DayDto;
import org.kdepo.solutions.mealplanner.server.dto.MealDto;
import org.kdepo.solutions.mealplanner.server.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.server.dto.WeekDto;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.model.Menu;
import org.kdepo.solutions.mealplanner.shared.model.Product;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Unit;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.IngredientsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MenusRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.UnitsRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/menus")
public class MenusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenusController.class);

    private static final String PK = "menu_id";

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private IngredientsRepository ingredientsRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private MenusRepository menusRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private UnitsRepository unitsRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping
    public String showMenusListPage(Model model) {
        LOGGER.trace("[WEB] GET /menus");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu list page: anonymous user cannot read menus list");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Prepare entities
        List<Menu> menus = menusRepository.getAllMenus();
        model.addAttribute("menus", menus);

        return "menus_list";
    }

    @GetMapping("/{id}")
    public String showMenuDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /menus/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu details page: anonymous user cannot read menu details");
            return "redirect:/menus";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Menu menu = menusRepository.getMenu(id);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show menu details page: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canReadMenu(userName, menu.getMenuId())) {
            LOGGER.warn("[WEB] Cannot show menu details page: user '{}' has no access to menu {}", userName, id);
            return "redirect:/menus";
        }

        // Prepare entities
        model.addAttribute("menu", menu);

        if (Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
            List<Day> daysList = daysRepository.getAllDaysFromMenu(menu.getMenuId());
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

        } else if (Constants.MenuType.DAYS_GROUPED_BY_WEEKS.equals(menu.getMenuTypeId())) {
            List<Week> weeksList = weeksRepository.getAllWeeksFromMenu(menu.getMenuId());
            List<WeekDto> weeks = new ArrayList<>();
            for (Week week : weeksList) {
                WeekDto weekDto = new WeekDto();
                weekDto.setWeekId(week.getWeekId());
                weekDto.setName(week.getName());

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
                weekDto.setDays(days);
                weeks.add(weekDto);
            }

            model.addAttribute("weeks", weeks);
        }

        return "menu_details";
    }

    @GetMapping("/create")
    public String showMenuCreationForm(Model model) {
        LOGGER.trace("[WEB] GET /menus/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu creation form: anonymous user cannot create menu");
            return "redirect:/menus";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateMenu(userName)) {
            LOGGER.warn("[WEB] Cannot show menu creation form: user '{}' cannot create menus", userName);
            return "redirect:/menus";
        }

        // Prepare entity with default values
        Menu menu = new Menu();
        menu.setMenuId(-1);
        menu.setMenuTypeId(Constants.MenuType.UNDEFINED);
        menu.setActive(false);

        model.addAttribute("menu", menu);

        return "menu_create";
    }

    @PostMapping("/create")
    public String acceptMenuCreationForm(@Valid Menu menu, BindingResult result) {
        LOGGER.trace("[WEB] POST /menus/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept menu creation form: anonymous user cannot create menus");
            return "redirect:/menus";
        }

        // Operation availability checks
        if (!controlService.canCreateMenu(userName)) {
            LOGGER.warn("[WEB] Cannot accept menu creation form: user '{}' cannot create menus", userName);
            return "redirect:/menus";
        }

        // Validate that provided data is correct
        String menuName = menu.getName();
        if (menuName == null || menuName.isEmpty()) {
            FieldError fieldError = new FieldError("menu", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "menu_create";
        }

        if (menuName.length() > 50) {
            FieldError fieldError = new FieldError("menu", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "menu_create";
        }

        // Adjust menu type id
        if (!Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())
                && !Constants.MenuType.DAYS_GROUPED_BY_WEEKS.equals(menu.getMenuTypeId())) {
            menu.setMenuTypeId(Constants.MenuType.UNDEFINED);
        }

        // Generate primary key for new entity
        Integer menuId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        menu.setMenuId(menuId);

        List<Menu> menus = menusRepository.getAllMenus();
        boolean active = menus.isEmpty();

        // Create entity
        Menu createdMenu = menusRepository.addMenu(
                menu.getMenuId(),
                menu.getMenuTypeId(),
                menu.getName(),
                active
        );

        // Register operation in system events log
        logService.registerMenuCreated(userName, createdMenu);

        return "redirect:/menus/" + menu.getMenuId();
    }

    @GetMapping("/{id}/update")
    public String showMenuModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /menus/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu modification form: anonymous user cannot modify menus");
            return "redirect:/menus/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Menu menu = menusRepository.getMenu(id);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show menu modification form: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyMenu(userName, menu.getMenuId())) {
            LOGGER.warn("[WEB] Cannot show menu modification form: user '{}' has no access to menu {} modification", userName, id);
            return "redirect:/menus/" + menu.getMenuId();
        }

        model.addAttribute("menu", menu);

        return "menu_update";
    }

    @PostMapping("/{id}/update")
    public String acceptMenuModificationForm(@Valid Menu menu, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /menus/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept menu modification form: anonymous user cannot modify menus");
            return "redirect:/menus/" + id;
        }

        Menu menuFromDb = menusRepository.getMenu(menu.getMenuId());
        if (menuFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept menu modification form: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyMenu(userName, menuFromDb.getMenuId())) {
            LOGGER.warn("[WEB] Cannot accept menu modification form: user '{}' has no access to menu {} modification", userName, id);
            return "redirect:/menus/" + menuFromDb.getMenuId();
        }

        // Validate that provided data is correct
        String menuName = menu.getName();
        if (menuName == null || menuName.isEmpty()) {
            FieldError nameFieldError = new FieldError("menu", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "menu_update";
        }

        if (menuName.length() > 50) {
            FieldError nameFieldError = new FieldError("menu", "name", "Название не может быть длиннее 50 символов!");
            result.addError(nameFieldError);
            return "menu_update";
        }

        menu.setActive(menuFromDb.getActive()); // Because this field is not present on the UI

        // Update entity
        menusRepository.updateMenu(
                menu.getMenuId(),
                menuFromDb.getMenuTypeId(),
                menu.getName(),
                menu.getActive()
        );

        // Register operation in system events log
        logService.registerMenuUpdated(userName, menuFromDb, menu);

        return "redirect:/menus/" + menu.getMenuId();
    }

    @GetMapping("/{id}/delete")
    public String showMenuDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /menus/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu deletion form: anonymous user cannot delete menus");
            return "redirect:/menus/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Menu menu = menusRepository.getMenu(id);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show menu deletion form: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteMenu(userName, menu.getMenuId())) {
            LOGGER.warn("[WEB] Cannot show menu deletion form: user '{}' has no access to menu {} deletion", userName, id);
            return "redirect:/menus/" + menu.getMenuId();
        }

        model.addAttribute("menu", menu);

        return "menu_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptMenuDeletionForm(@PathVariable Integer id) {
        LOGGER.trace("[WEB] POST /menus/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept menu deletion form: anonymous user cannot delete menus");
            return "redirect:/menus/" + id;
        }

        Menu menuFromDb = menusRepository.getMenu(id);
        if (menuFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept menu deletion form: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canDeleteMenu(userName, menuFromDb.getMenuId())) {
            LOGGER.warn("[WEB] Cannot accept menu deletion form: user '{}' has no access to menu {} deletion", userName, id);
            return "redirect:/menus/" + menuFromDb.getMenuId();
        }

        // Delete entity
        menusRepository.deleteMenu(menuFromDb.getMenuId());

        if (menuFromDb.getActive()) {
            List<Menu> allMenus = menusRepository.getAllMenus();
            if (!allMenus.isEmpty()) {
                Menu nextActiveMenu = allMenus.get(0);
                nextActiveMenu.setActive(true);
                menusRepository.updateMenu(
                        nextActiveMenu.getMenuId(),
                        nextActiveMenu.getMenuTypeId(),
                        nextActiveMenu.getName(),
                        nextActiveMenu.getActive()
                );
            }
        }

        // Register operation in system events log
        logService.registerMenuDeleted(userName, menuFromDb.getMenuId());

        return "redirect:/menus";
    }

    @GetMapping("/{id}/active")
    public String setMenuActive(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /menus/{}/active", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot set menu active: anonymous user cannot modify menus");
            return "redirect:/menus/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Menu menu = menusRepository.getMenu(id);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot set menu active: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canModifyMenu(userName, menu.getMenuId())) {
            LOGGER.warn("[WEB] Cannot set menu active: user '{}' has no access to menu {} modification", userName, id);
            return "redirect:/menus/" + menu.getMenuId();
        }

        List<Menu> allMenus = menusRepository.getAllMenus();
        List<Menu> activeMenus = allMenus.stream()
                .filter(Menu::getActive)
                .toList();

        for (Menu activeMenu : activeMenus) {
            if (!menu.getMenuId().equals(activeMenu.getMenuId())) {
                activeMenu.setActive(false);
                menusRepository.updateMenu(
                        activeMenu.getMenuId(),
                        activeMenu.getMenuTypeId(),
                        activeMenu.getName(),
                        activeMenu.getActive()
                );
            }
        }

        if (!menu.getActive()) {
            menu.setActive(true);
            menusRepository.updateMenu(
                    menu.getMenuId(),
                    menu.getMenuTypeId(),
                    menu.getName(),
                    menu.getActive()
            );
        }

        model.addAttribute("menu", menu);

        return "redirect:/menus/" + id;
    }

    @GetMapping("/{id}/products")
    public String showMenuProductsListPage(@PathVariable Integer id,
                                           Model model,
                                           @RequestParam(value = "day_id", required = false) Integer dayId,
                                           @RequestParam(value = "week_id", required = false) Integer weekId) {
        LOGGER.trace("[WEB] GET /menus/{}/products", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show menu products list page: anonymous user cannot read menu products list");
            return "redirect:/menus";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Menu menu = menusRepository.getMenu(id);
        if (menu == null) {
            LOGGER.warn("[WEB] Cannot show menu products list page: menu {} was not found", id);
            return "redirect:/menus";
        }

        if (!controlService.canReadMenu(userName, menu.getMenuId())) {
            LOGGER.warn("[WEB] Cannot show menu products list page: user '{}' has no access to menu {}", userName, id);
            return "redirect:/menus";
        }

        // Prepare entities
        model.addAttribute("menu", menu);

        // Collect recipes list
        List<Recipe> recipes = new ArrayList<>();
        if (dayId != null && weekId == null) {
            // Collect recipes for one day only
            Day day = daysRepository.getDay(dayId);
            if (day == null) {
                LOGGER.warn("[WEB] Cannot show menu products list page: day {} was not found", dayId);
                return "redirect:/menus";
            }

            if (!controlService.canReadDay(userName, day.getDayId())) {
                LOGGER.warn("[WEB] Cannot show menu products list page: user '{}' has no access to day {}", userName, dayId);
                return "redirect:/menus";
            }

            if (!menu.getMenuId().equals(day.getMenuId())) {
                LOGGER.warn("[WEB] Cannot show menu products list page: day {} doesn't match menu {}", dayId, menu.getMenuId());
                return "redirect:/menus";
            }

            List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
            for (Meal meal : meals) {
                recipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
            }

        } else if (dayId == null && weekId != null) {
            // Collect recipes for all days from week
            Week week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot show menu products list page: week {} was not found", weekId);
                return "redirect:/menus";
            }

            if (!controlService.canReadWeek(userName, week.getWeekId())) {
                LOGGER.warn("[WEB] Cannot show menu products list page: user '{}' has no access to week {}", userName, weekId);
                return "redirect:/menus";
            }

            if (!menu.getMenuId().equals(week.getMenuId())) {
                LOGGER.warn("[WEB] Cannot show menu products list page: week {} doesn't match menu {}", weekId, menu.getMenuId());
                return "redirect:/menus";
            }

            List<Day> days = daysRepository.getAllDaysFromWeek(week.getWeekId());
            for (Day day : days) {
                List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
                for (Meal meal : meals) {
                    recipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
                }
            }

        } else if (dayId == null && weekId == null) {
            // Collect recipes for all menu
            if (Constants.MenuType.DAYS_WITHOUT_GROUPING.equals(menu.getMenuTypeId())) {
                List<Day> days = daysRepository.getAllDaysFromMenu(menu.getMenuId());
                for (Day day : days) {
                    List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
                    for (Meal meal : meals) {
                        recipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
                    }
                }
            } else if (Constants.MenuType.DAYS_GROUPED_BY_WEEKS.equals(menu.getMenuTypeId())) {
                List<Week> weeks = weeksRepository.getAllWeeksFromMenu(menu.getMenuId());
                for (Week week : weeks) {
                    List<Day> days = daysRepository.getAllDaysFromWeek(week.getWeekId());
                    for (Day day : days) {
                        List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
                        for (Meal meal : meals) {
                            recipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
                        }
                    }
                }
            }

        } else {
            LOGGER.warn("[WEB] Cannot show menu products list page: too many parameters provided {} {}", dayId, weekId);
            return "redirect:/menus";
        }

        // Collect ingredients from recipes
        List<Ingredient> ingredients = new ArrayList<>();
        for (Recipe recipe : recipes) {
            ingredients.addAll(ingredientsRepository.getAllIngredientsFromRecipe(recipe.getRecipeId()));
        }

        // Collect products from ingredients
        // <ProductId <UnitId, Amount>>
        Map<Integer, Map<Integer, Integer>> productsUnitsAmountsIdsMap = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            Integer productId = ingredient.getProductId();
            Map<Integer, Integer> unitsMap = productsUnitsAmountsIdsMap.get(productId);
            if (unitsMap == null) {
                unitsMap = new HashMap<>();
                unitsMap.put(ingredient.getUnitId(), ingredient.getAmount());
                productsUnitsAmountsIdsMap.put(productId, unitsMap);
            } else {
                Integer amount = unitsMap.get(ingredient.getUnitId());
                if (amount == null) {
                    unitsMap.put(ingredient.getUnitId(), ingredient.getAmount());
                } else {
                    amount = amount + ingredient.getAmount();
                    unitsMap.put(ingredient.getUnitId(), amount);
                }
            }
        }

        Map<Integer, Unit> cachedUnitsMap = new HashMap<>();
        Map<Product, Map<Unit, Integer>> productsMap = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> productsUnitsAmountsMapEntry : productsUnitsAmountsIdsMap.entrySet()) {
            Product product = productsRepository.getProduct(productsUnitsAmountsMapEntry.getKey());

            Map<Unit, Integer> unitsAmountsMap = new HashMap<>();
            Map<Integer, Integer> unitsIdsAmountMap = productsUnitsAmountsMapEntry.getValue();
            for (Map.Entry<Integer, Integer> unitIdAmountEntry : unitsIdsAmountMap.entrySet()) {
                Unit unit = cachedUnitsMap.get(unitIdAmountEntry.getKey());
                if (unit == null) {
                    unit = unitsRepository.getUnit(unitIdAmountEntry.getKey());
                    cachedUnitsMap.put(unitIdAmountEntry.getKey(), unit);
                }
                unitsAmountsMap.put(unit, unitIdAmountEntry.getValue());
            }

            productsMap.put(product, unitsAmountsMap);
        }
        model.addAttribute("productsMap", productsMap);

        return "menus_products";
    }
}
