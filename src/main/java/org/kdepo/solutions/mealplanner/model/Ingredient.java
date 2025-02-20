package org.kdepo.solutions.mealplanner.model;

import java.util.Objects;

public class Ingredient {

    private Integer ingredientId;
    private String name;
    private Integer recipeId;
    private Integer productId;
    private Integer amount;
    private Integer unitId;

    public Integer getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientId=" + ingredientId +
                ", name='" + name + '\'' +
                ", recipeId=" + recipeId +
                ", productId=" + productId +
                ", amount=" + amount +
                ", unitId=" + unitId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(ingredientId, that.ingredientId)
                && Objects.equals(name, that.name)
                && Objects.equals(recipeId, that.recipeId)
                && Objects.equals(productId, that.productId)
                && Objects.equals(amount, that.amount)
                && Objects.equals(unitId, that.unitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientId, name, recipeId, productId, amount, unitId);
    }
}
