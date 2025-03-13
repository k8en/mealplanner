package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductsRepository {

    Product addProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs);

    void deleteProduct(Integer productId);

    List<Product> getAllProducts();

    Product getProduct(Integer productId);

    boolean isUsed(Integer productId);

    void updateProduct(Integer productId, String name, String description, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs);


}
