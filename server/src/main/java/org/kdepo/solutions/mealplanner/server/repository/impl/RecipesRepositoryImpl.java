package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.server.tools.DbUtils;
import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RecipesRepositoryImpl implements RecipesRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipesRepositoryImpl.class);

    private static final BigDecimal DECIMAL_MULTIPLIER = BigDecimal.valueOf(10000L);
    private static final Integer DECIMAL_SCALE = 5;

    private static final String SQL_ADD_RECIPE = "INSERT INTO recipes (recipe_id, instruction_type_id, name, description, source, portions, weight, calories, proteins, fats, carbs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ADD_RECIPE_TO_MEAL = "INSERT INTO meals_contents (meal_id, recipe_id, order_number) VALUES (?, ?, ?);";
    private static final String SQL_DELETE_RECIPE = "DELETE FROM recipes WHERE recipe_id = ?";
    private static final String SQL_DELETE_RECIPE_FROM_MEAL = "DELETE FROM meals_contents WHERE recipe_id = ? AND meal_id = ?";
    private static final String SQL_GET_ALL_RECIPES = "SELECT * FROM recipes ORDER BY name ASC";
    private static final String SQL_GET_ALL_RECIPES_FILTERED = ""
            + "SELECT r.* FROM recipes r"
            + " WHERE 1=1"
            + " /*FILTER*/"
            + " ORDER BY r.name ASC";
    private static final String SQL_GET_ALL_RECIPES_FROM_MEAL = "SELECT r.recipe_id, r.instruction_type_id, r.name, r.description, r.source, r.portions, r.weight, r.calories, r.proteins, r.fats, r.carbs FROM meals_contents mc JOIN recipes r ON r.recipe_id = mc.recipe_id WHERE mc.meal_id = ? ORDER BY mc.order_number ASC";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM meals_contents WHERE meal_id = ?";
    private static final String SQL_GET_RECIPE = "SELECT * FROM recipes WHERE recipe_id = ?";
    private static final String SQL_IS_USED = "SELECT recipe_id FROM meals_contents WHERE recipe_id = ? LIMIT 1";
    private static final String SQL_UPDATE_MEALS_CONTENTS = "UPDATE meals_contents SET order_number = ? WHERE meal_id = ? AND recipe_id = ?";
    private static final String SQL_UPDATE_RECIPE = "UPDATE recipes SET instruction_type_id = ?, name = ?, description = ?, source = ?, portions = ?, weight = ?, calories = ?, proteins = ?, fats = ?, carbs = ? WHERE recipe_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private IngredientsRepositoryImpl ingredientsRepository;

    @Autowired
    private TagsRepositoryImpl tagsRepository;

    public RecipesRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Recipe addRecipe(Integer recipeId, Integer instructionTypeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        LOGGER.trace("[DBR][addRecipe] Invoked with parameters: recipeId={}, instructionTypeId={}, name={}, description={}, source={}, portions={}, weight={}, calories={}, proteins={}, fats={}, carbs={}",
                recipeId, instructionTypeId, name, description, source, portions, weight, calories, proteins, fats, carbs
        );

        BigDecimal weightConvertedToDb = weight.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(
                SQL_ADD_RECIPE,
                ps -> {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, instructionTypeId);
                    ps.setString(3, name);
                    ps.setString(4, description);
                    ps.setString(5, source);
                    ps.setInt(6, portions);
                    ps.setBigDecimal(7, weightConvertedToDb);
                    ps.setBigDecimal(8, caloriesConvertedToDb);
                    ps.setBigDecimal(9, proteinsConvertedToDb);
                    ps.setBigDecimal(10, fatsConvertedToDb);
                    ps.setBigDecimal(11, carbsConvertedToDb);
                }
        );

        return getRecipe(recipeId);
    }

    @Override
    public void addRecipeToMeal(Integer recipeId, Integer mealId, Integer orderNumber) {
        LOGGER.trace("[DBR][addRecipeToMeal] Invoked with parameters: recipeId={}, mealId={}, orderNumber={}",
                recipeId, mealId, orderNumber
        );
        jdbcTemplate.update(
                SQL_ADD_RECIPE_TO_MEAL,
                ps -> {
                    ps.setInt(1, mealId);
                    ps.setInt(2, recipeId);
                    ps.setInt(3, orderNumber);
                }
        );
    }

    @Override
    public void deleteRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][deleteRecipe] Invoked with parameters: recipeId={}", recipeId);
        jdbcTemplate.update(
                SQL_DELETE_RECIPE,
                ps -> ps.setInt(1, recipeId)
        );
    }

    @Override
    public void deleteRecipeFromMeal(Integer recipeId, Integer mealId) {
        LOGGER.trace("[DBR][deleteRecipeFromMeal] Invoked with parameters: recipeId={}, mealId={}",
                recipeId, mealId
        );
        jdbcTemplate.update(
                SQL_DELETE_RECIPE_FROM_MEAL,
                ps -> {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, mealId);
                }
        );
    }

    @Override
    public List<Recipe> getAllRecipes() {
        LOGGER.trace("[DBR][getAllRecipes] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_RECIPES,
                rs -> {
                    List<Recipe> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public List<Recipe> getAllRecipes(List<Integer> products, List<Integer> tags) {
        LOGGER.trace("[DBR][getAllRecipes] Invoked with parameters: products={}, tags={}",
                products, tags
        );

        String filter = "";
        if (!products.isEmpty()) {
            filter = filter + " AND r.recipe_id IN (SELECT recipe_id FROM ingredients WHERE product_id IN (" + DbUtils.toArray(products) + "))";
        }
        if (!tags.isEmpty()) {
            filter = filter + " AND r.recipe_id IN (SELECT recipe_id FROM recipes_tags WHERE tag_id IN (" + DbUtils.toArray(tags) + "))";
        }

        String query = SQL_GET_ALL_RECIPES_FILTERED.replace("/*FILTER*/", filter);

        return jdbcTemplate.query(
                query,
                rs -> {
                    List<Recipe> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public List<Recipe> getAllRecipesFromMeal(Integer mealId) {
        LOGGER.trace("[DBR][getAllRecipesFromMeal] Invoked with parameters: mealId={}", mealId);
        return jdbcTemplate.query(
                SQL_GET_ALL_RECIPES_FROM_MEAL,
                ps -> ps.setInt(1, mealId),
                rs -> {
                    List<Recipe> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Integer getOrderNumber(Integer mealId) {
        LOGGER.trace("[DBR][getOrderNumber] Invoked with parameters: mealId={}", mealId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                ps -> ps.setInt(1, mealId),
                rs -> {
                    Integer orderNumber = null;
                    if (rs.next()) {
                        orderNumber = Integer.parseInt(rs.getString("order_number"));
                    }
                    return orderNumber;
                }
        );
    }

    @Override
    public Recipe getRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][getRecipe] Invoked with parameters: recipeId={}", recipeId);
        Recipe recipe = jdbcTemplate.query(
                SQL_GET_RECIPE,
                ps -> ps.setInt(1, recipeId),
                rs -> {
                    Recipe result = null;
                    if (rs.next()) {
                        result = convert(rs);
                    }
                    return result;
                }
        );

        if (recipe != null) {
            List<Ingredient> ingredientsList = ingredientsRepository.getAllIngredientsFromRecipe(recipeId);
            recipe.setIngredientsList(ingredientsList);

            List<Tag> tagsList = tagsRepository.getAllTagsForRecipe(recipeId);
            recipe.setTagsList(tagsList);
        }

        return recipe;
    }

    @Override
    public boolean isUsed(Integer recipeId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: recipeId={}", recipeId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, recipeId),
                ResultSet::next
        ));
    }

    @Override
    public void updateMealsContents(Integer mealId, Integer recipeId, Integer orderNumber) {
        LOGGER.trace("[DBR][updateMealsContents] Invoked with parameters: mealId={}, recipeId={}, orderNumber={}",
                mealId, recipeId, orderNumber
        );
        jdbcTemplate.update(
                SQL_UPDATE_MEALS_CONTENTS,
                ps -> {
                    ps.setInt(1, orderNumber);
                    ps.setInt(2, mealId);
                    ps.setInt(3, recipeId);
                }
        );
    }

    @Override
    public void updateRecipe(Integer recipeId, Integer instructionTypeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        LOGGER.trace("[DBR][updateRecipe] Invoked with parameters: recipeId={}, instructionTypeId={}, name={}, description={}, source={}, portions={}, weight={}, calories={}, proteins={}, fats={}, carbs={}",
                recipeId, instructionTypeId, name, description, source, portions, weight, calories, proteins, fats, carbs
        );

        BigDecimal weightConvertedToDb = weight.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(
                SQL_UPDATE_RECIPE,
                ps -> {
                    ps.setInt(1, instructionTypeId);
                    ps.setString(2, name);
                    ps.setString(3, description);
                    ps.setString(4, source);
                    ps.setInt(5, portions);
                    ps.setBigDecimal(6, weightConvertedToDb);
                    ps.setBigDecimal(7, caloriesConvertedToDb);
                    ps.setBigDecimal(8, proteinsConvertedToDb);
                    ps.setBigDecimal(9, fatsConvertedToDb);
                    ps.setBigDecimal(10, carbsConvertedToDb);
                    ps.setInt(11, recipeId);
                }
        );
    }

    private Recipe convert(ResultSet rs) throws SQLException {
        Integer recipeId = rs.getInt("recipe_id");
        Integer instructionTypeId = rs.getInt("instruction_type_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String source = rs.getString("source");
        Integer portions = rs.getInt("portions");

        BigDecimal weight = rs.getBigDecimal("weight");
        BigDecimal calories = rs.getBigDecimal("calories");
        BigDecimal proteins = rs.getBigDecimal("proteins");
        BigDecimal fats = rs.getBigDecimal("fats");
        BigDecimal carbs = rs.getBigDecimal("carbs");

        BigDecimal weightConvertedFromDb = weight
                .divide(DECIMAL_MULTIPLIER, DECIMAL_SCALE, RoundingMode.DOWN)
                .stripTrailingZeros();
        BigDecimal caloriesConvertedFromDb = calories
                .divide(DECIMAL_MULTIPLIER, DECIMAL_SCALE, RoundingMode.DOWN)
                .stripTrailingZeros();
        BigDecimal proteinsConvertedFromDb = proteins
                .divide(DECIMAL_MULTIPLIER, DECIMAL_SCALE, RoundingMode.DOWN)
                .stripTrailingZeros();
        BigDecimal fatsConvertedFromDb = fats
                .divide(DECIMAL_MULTIPLIER, DECIMAL_SCALE, RoundingMode.DOWN)
                .stripTrailingZeros();
        BigDecimal carbsConvertedFromDb = carbs
                .divide(DECIMAL_MULTIPLIER, DECIMAL_SCALE, RoundingMode.DOWN)
                .stripTrailingZeros();

        Recipe recipe = new Recipe();
        recipe.setRecipeId(recipeId);
        recipe.setInstructionTypeId(instructionTypeId);
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setSource(source);
        recipe.setPortions(portions);
        recipe.setWeight(weightConvertedFromDb);
        recipe.setCalories(caloriesConvertedFromDb);
        recipe.setProteins(proteinsConvertedFromDb);
        recipe.setFats(fatsConvertedFromDb);
        recipe.setCarbs(carbsConvertedFromDb);

        return recipe;
    }
}
