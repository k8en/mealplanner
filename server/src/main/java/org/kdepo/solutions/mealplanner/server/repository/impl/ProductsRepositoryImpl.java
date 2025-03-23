package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Product;
import org.kdepo.solutions.mealplanner.shared.repository.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ProductsRepositoryImpl implements ProductsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductsRepositoryImpl.class);

    private static final BigDecimal DECIMAL_MULTIPLIER = BigDecimal.valueOf(10000L);
    private static final Integer DECIMAL_SCALE = 5;

    private static final String SQL_ADD_PRODUCT = "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_PRODUCT = "DELETE FROM products WHERE product_id = ?";
    private static final String SQL_GET_ALL_PRODUCTS = "SELECT * FROM products ORDER BY name ASC";
    private static final String SQL_GET_PRODUCT = "SELECT * FROM products WHERE product_id = ?";
    private static final String SQL_IS_USED = "SELECT product_id FROM ingredients WHERE product_id = ? LIMIT 1";
    private static final String SQL_UPDATE_PRODUCT = "UPDATE products SET name = ?, description = ?, calories = ?, proteins = ?, fats = ?, carbs = ? WHERE product_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public ProductsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Product addProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        LOGGER.trace("[DBR][addProduct] Invoked with parameters: productId={}, name={}, description={}, calories={}, proteins={}, fats={}, carbs={}",
                productId, name, description, calories, proteins, fats, carbs
        );

        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(
                SQL_ADD_PRODUCT,
                ps -> {
                    ps.setInt(1, productId);
                    ps.setString(2, name);
                    ps.setString(3, description);
                    ps.setBigDecimal(4, caloriesConvertedToDb);
                    ps.setBigDecimal(5, proteinsConvertedToDb);
                    ps.setBigDecimal(6, fatsConvertedToDb);
                    ps.setBigDecimal(7, carbsConvertedToDb);
                }
        );

        return getProduct(productId);
    }

    @Override
    public void deleteProduct(Integer productId) {
        LOGGER.trace("[DBR][deleteProduct] Invoked with parameters: productId={}", productId);
        jdbcTemplate.update(
                SQL_DELETE_PRODUCT,
                ps -> ps.setInt(1, productId)
        );
    }

    @Override
    public List<Product> getAllProducts() {
        LOGGER.trace("[DBR][getAllProducts] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_PRODUCTS,
                rs -> {
                    List<Product> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Product getProduct(Integer productId) {
        LOGGER.trace("[DBR][getProduct] Invoked with parameters: productId={}", productId);
        return jdbcTemplate.query(
                SQL_GET_PRODUCT,
                rs -> {
                    Product product = null;
                    if (rs.next()) {
                        product = convert(rs);
                    }
                    return product;
                }
        );
    }

    @Override
    public boolean isUsed(Integer productId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: productId={}", productId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, productId),
                ResultSet::next
        ));
    }

    @Override
    public void updateProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs) {
        LOGGER.trace("[DBR][updateProduct] Invoked with parameters: productId={}, name={}, description={}, calories={}, proteins={}, fats={}, carbs={}",
                productId, name, description, calories, proteins, fats, carbs
        );

        BigDecimal caloriesConvertedToDb = calories.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal proteinsConvertedToDb = proteins.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal fatsConvertedToDb = fats.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);
        BigDecimal carbsConvertedToDb = carbs.stripTrailingZeros().multiply(DECIMAL_MULTIPLIER);

        jdbcTemplate.update(
                SQL_UPDATE_PRODUCT,
                ps -> {
                    ps.setString(1, name);
                    ps.setString(2, description);
                    ps.setBigDecimal(3, caloriesConvertedToDb);
                    ps.setBigDecimal(4, proteinsConvertedToDb);
                    ps.setBigDecimal(5, fatsConvertedToDb);
                    ps.setBigDecimal(6, carbsConvertedToDb);
                    ps.setInt(7, productId);
                }
        );
    }

    private Product convert(ResultSet rs) throws SQLException {
        Integer productId = rs.getInt("product_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal calories = rs.getBigDecimal("calories");
        BigDecimal proteins = rs.getBigDecimal("proteins");
        BigDecimal fats = rs.getBigDecimal("fats");
        BigDecimal carbs = rs.getBigDecimal("carbs");

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
}
