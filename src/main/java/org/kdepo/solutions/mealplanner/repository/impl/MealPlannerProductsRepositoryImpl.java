package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.repository.MealPlannerProductsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
public class MealPlannerProductsRepositoryImpl implements MealPlannerProductsRepository {

    private static final BigDecimal DECIMAL_MULTIPLIER = BigDecimal.valueOf(10000L);
    private static final Integer DECIMAL_SCALE = 5;

    private static final String SQL_ADD_PRODUCT = "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_PRODUCT = "DELETE FROM products WHERE product_id = ?";
    private static final String SQL_GET_ALL_PRODUCTS = "SELECT * FROM products ORDER BY name ASC";
    private static final String SQL_GET_PRODUCT = "SELECT * FROM products WHERE product_id = ?";
    private static final String SQL_IS_USED = "SELECT product_id FROM ingredients WHERE product_id = ? LIMIT 1";
    private static final String SQL_UPDATE_PRODUCT = "UPDATE products SET name = ?, description = ?, calories = ?, proteins = ?, fats = ?, carbs = ? WHERE product_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MealPlannerProductsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Product addProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[ML][ProductDao][addProduct] Invoked with parameters:"
                + " productId=" + productId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(SQL_ADD_PRODUCT, productId, name, description, caloriesConvertedToDb, proteinsConvertedToDb, fatsConvertedToDb, carbsConvertedToDb);

        return getProduct(productId);
    }

    @Override
    public void deleteProduct(Integer productId) {
        System.out.println("[ML][ProductDao][deleteProduct] Invoked with parameters: productId=" + productId);
        jdbcTemplate.update(SQL_DELETE_PRODUCT, productId);
    }

    @Override
    public List<Product> getAllProducts() {
        System.out.println("[ML][ProductDao][getAllProducts] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_PRODUCTS,
                (resultSet, rowNum) -> {
                    Integer productId = resultSet.getInt("product_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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

                    Product product = new Product();
                    product.setProductId(productId);
                    product.setName(name);
                    product.setDescription(description);
                    product.setCalories(caloriesConvertedFromDb);
                    product.setProteins(proteinsConvertedFromDb);
                    product.setFats(fatsConvertedFromDb);
                    product.setCarbs(carbsConvertedFromDb);

                    return product;
                }
        );
    }

    @Override
    public Product getProduct(Integer productId) {
        System.out.println("[ML][ProductDao][getProduct] Invoked with parameters: productId=" + productId);
        return jdbcTemplate.query(
                SQL_GET_PRODUCT,
                resultSet -> {
                    //Integer productId = resultSet.getInt("product_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    BigDecimal calories = resultSet.getBigDecimal("calories");
                    BigDecimal proteins = resultSet.getBigDecimal("proteins");
                    BigDecimal fats = resultSet.getBigDecimal("fats");
                    BigDecimal carbs = resultSet.getBigDecimal("carbs");

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

                    Product product = new Product();
                    product.setProductId(productId);
                    product.setName(name);
                    product.setDescription(description);
                    product.setCalories(caloriesConvertedFromDb);
                    product.setProteins(proteinsConvertedFromDb);
                    product.setFats(fatsConvertedFromDb);
                    product.setCarbs(carbsConvertedFromDb);

                    return product;
                },
                productId
        );
    }

    @Override
    public boolean isUsed(Integer productId) {
        System.out.println("[ML][ProductDao][isUsed] Invoked with parameters: productId=" + productId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("product_id");
                },
                productId
        );
        return objectId != null;
    }

    @Override
    public void updateProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        System.out.println("[ML][ProductDao][updateProduct] Invoked with parameters:"
                + " productId=" + productId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", calories=" + calories
                + ", proteins=" + proteins
                + ", fats=" + fats
                + ", carbs=" + carbs
        );

        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(SQL_UPDATE_PRODUCT, name, description, caloriesConvertedToDb, proteinsConvertedToDb, fatsConvertedToDb, carbsConvertedToDb, productId);
    }
}
