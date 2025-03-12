package org.kdepo.solutions.mealplanner.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

    private Integer productId;
    private String name;
    private String description;
    private BigDecimal calories;
    private BigDecimal proteins;
    private BigDecimal fats;
    private BigDecimal carbs;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = calories;
    }

    public BigDecimal getProteins() {
        return proteins;
    }

    public void setProteins(BigDecimal proteins) {
        this.proteins = proteins;
    }

    public BigDecimal getFats() {
        return fats;
    }

    public void setFats(BigDecimal fats) {
        this.fats = fats;
    }

    public BigDecimal getCarbs() {
        return carbs;
    }

    public void setCarbs(BigDecimal carbs) {
        this.carbs = carbs;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", calories=" + calories +
                ", proteins=" + proteins +
                ", fats=" + fats +
                ", carbs=" + carbs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId)
                && Objects.equals(name, product.name)
                && Objects.equals(description, product.description)
                && Objects.equals(calories, product.calories)
                && Objects.equals(proteins, product.proteins)
                && Objects.equals(fats, product.fats)
                && Objects.equals(carbs, product.carbs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, description, calories, proteins, fats, carbs);
    }
}
