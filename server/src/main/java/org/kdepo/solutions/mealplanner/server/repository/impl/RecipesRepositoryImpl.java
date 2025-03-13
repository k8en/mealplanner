package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.server.tools.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
public class RecipesRepositoryImpl implements RecipesRepository {

    private static final BigDecimal DECIMAL_MULTIPLIER = BigDecimal.valueOf(10000L);
    private static final Integer DECIMAL_SCALE = 5;

    private static final String SQL_ADD_RECIPE = "INSERT INTO recipes (recipe_id, name, description, source, portions, weight, calories, proteins, fats, carbs) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ADD_RECIPE_TO_MEAL = "INSERT INTO meals_contents (meal_id, recipe_id, order_number) VALUES (?, ?, ?);";
    private static final String SQL_DELETE_RECIPE = "DELETE FROM recipes WHERE recipe_id = ?";
    private static final String SQL_DELETE_RECIPE_FROM_MEAL = "DELETE FROM meals_contents WHERE recipe_id = ? AND meal_id = ?";
    private static final String SQL_GET_ALL_RECIPES = "SELECT * FROM recipes ORDER BY name ASC";
    private static final String SQL_GET_ALL_RECIPES_FILTERED = ""
            + "SELECT r.* FROM recipes r"
            + " WHERE 1=1"
            + " /*FILTER*/"
            + " ORDER BY r.name ASC";
    private static final String SQL_GET_ALL_RECIPES_FROM_MEAL = "SELECT r.recipe_id, r.name, r.description, r.source, r.portions, r.weight, r.calories, r.proteins, r.fats, r.carbs FROM meals_contents mc JOIN recipes r ON r.recipe_id = mc.recipe_id WHERE mc.meal_id = ? ORDER BY mc.order_number ASC";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM meals_contents WHERE meal_id = ?";
    private static final String SQL_GET_RECIPE = "SELECT * FROM recipes WHERE recipe_id = ?";
    private static final String SQL_IS_USED = "SELECT recipe_id FROM meals_contents WHERE recipe_id = ? LIMIT 1";
    private static final String SQL_UPDATE_MEALS_CONTENTS = "UPDATE meals_contents SET order_number = ? WHERE meal_id = ? AND recipe_id = ?";
    private static final String SQL_UPDATE_RECIPE = "UPDATE recipes SET name = ?, description = ?, source = ?, portions = ?, weight = ?, calories = ?, proteins = ?, fats = ?, carbs = ? WHERE recipe_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private IngredientsRepositoryImpl ingredientsRepository;

    @Autowired
    private TagsRepositoryImpl tagsRepository;

    public RecipesRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Recipe addRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[RecipeDao][addRecipe] Invoked with parameters:"
                + " recipeId=" + recipeId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", source='" + source + "'"
                + ", portions=" + portions
                + ", weight=" + weight
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        BigDecimal weightConvertedToDb = weight.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(SQL_ADD_RECIPE, recipeId, name, description, source, portions, weightConvertedToDb, caloriesConvertedToDb, proteinsConvertedToDb, fatsConvertedToDb, carbsConvertedToDb);

        return getRecipe(recipeId);
    }

    @Override
    public void addRecipeToMeal(Integer recipeId, Integer mealId, Integer orderNumber) {
        System.out.println("[RecipeDao][addRecipeToMeal] Invoked with parameters:"
                + " recipeId=" + recipeId
                + ", mealId=" + mealId
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_ADD_RECIPE_TO_MEAL, mealId, recipeId, orderNumber);
    }

    @Override
    public void deleteRecipe(Integer recipeId) {
        System.out.println("[RecipeDao][deleteRecipe] Invoked with parameters: recipeId=" + recipeId);
        jdbcTemplate.update(SQL_DELETE_RECIPE, recipeId);
    }

    @Override
    public void deleteRecipeFromMeal(Integer recipeId, Integer mealId) {
        System.out.println("[RecipeDao][deleteRecipeFromMeal] Invoked with parameters:"
                + " recipeId=" + recipeId
                + ", mealId=" + mealId
        );
        jdbcTemplate.update(SQL_DELETE_RECIPE_FROM_MEAL, recipeId, mealId);
    }

    @Override
    public List<Recipe> getAllRecipes() {
        System.out.println("[RecipeDao][getAllRecipes] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_RECIPES,
                (resultSet, rowNum) -> {
                    Integer recipeId = resultSet.getInt("recipe_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String source = resultSet.getString("source");
                    Integer portions = resultSet.getInt("portions");

                    BigDecimal weight = resultSet.getBigDecimal("weight");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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
        );
    }

    @Override
    public List<Recipe> getAllRecipes(List<Integer> products, List<Integer> tags) {
        System.out.println("[RecipeDao][getAllRecipes] Invoked with parameters:"
                + " products=" + products
                + ", tags=" + tags
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
                (resultSet, rowNum) -> {
                    Integer recipeId = resultSet.getInt("recipe_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String source = resultSet.getString("source");
                    Integer portions = resultSet.getInt("portions");

                    BigDecimal weight = resultSet.getBigDecimal("weight");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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
        );
    }

    @Override
    public List<Recipe> getAllRecipesFromMeal(Integer mealId) {
        System.out.println("[RecipeDao][getAllRecipesFromMeal] Invoked with parameters: mealId=" + mealId);
        return jdbcTemplate.query(
                SQL_GET_ALL_RECIPES_FROM_MEAL,
                (resultSet, rowNum) -> {
                    Integer recipeId = resultSet.getInt("recipe_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String source = resultSet.getString("source");
                    Integer portions = resultSet.getInt("portions");

                    BigDecimal weight = resultSet.getBigDecimal("weight");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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
                },
                mealId
        );
    }

    @Override
    public Integer getOrderNumber(Integer mealId) {
        System.out.println("[RecipeDao][getOrderNumber] Invoked with parameters: mealId=" + mealId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                resultSet -> {
                    return resultSet.getInt("order_number");
                },
                mealId
        );
    }

    @Override
    public Recipe getRecipe(Integer recipeId) {
        System.out.println("[RecipeDao][getRecipe] Invoked with parameters: recipeId=" + recipeId);
        Recipe recipe = jdbcTemplate.query(
                SQL_GET_RECIPE,
                resultSet -> {
                    //Integer recipeId = resultSet.getInt("recipe_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String source = resultSet.getString("source");
                    Integer portions = resultSet.getInt("portions");

                    BigDecimal weight = resultSet.getBigDecimal("weight");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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

                    Recipe recipeFromDb = new Recipe();
                    recipeFromDb.setRecipeId(recipeId);
                    recipeFromDb.setName(name);
                    recipeFromDb.setDescription(description);
                    recipeFromDb.setSource(source);
                    recipeFromDb.setPortions(portions);
                    recipeFromDb.setWeight(weightConvertedFromDb);
                    recipeFromDb.setCalories(caloriesConvertedFromDb);
                    recipeFromDb.setProteins(proteinsConvertedFromDb);
                    recipeFromDb.setFats(fatsConvertedFromDb);
                    recipeFromDb.setCarbs(carbsConvertedFromDb);

                    return recipeFromDb;
                },
                recipeId
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
        System.out.println("[RecipeDao][isUsed] Invoked with parameters: recipeId=" + recipeId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("recipe_id");
                },
                recipeId
        );
        return objectId != null;
    }

    @Override
    public void updateMealsContents(Integer mealId, Integer recipeId, Integer orderNumber) {
        System.out.println("[RecipeDao][updateMealsContents] Invoked with parameters:"
                + " mealId=" + mealId
                + ", recipeId=" + recipeId
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_UPDATE_MEALS_CONTENTS, orderNumber, mealId, recipeId);
    }

    @Override
    public void updateRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[RecipeDao][updateRecipe] Invoked with parameters:"
                + " recipeId=" + recipeId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", source='" + source + "'"
                + ", portions=" + portions
                + ", weight=" + weight
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        BigDecimal weightConvertedToDb = weight.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(SQL_UPDATE_RECIPE, name, description, source, portions, weightConvertedToDb, caloriesConvertedToDb, proteinsConvertedToDb, fatsConvertedToDb, carbsConvertedToDb, recipeId);
    }
}
