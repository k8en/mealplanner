package org.kdepo.solutions.mealplanner.shared.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Recipe {

    private Integer recipeId;
    private String name;
    private String description;
    private String source;
    private Integer portions;
    private BigDecimal weight;
    private BigDecimal calories;
    private BigDecimal proteins;
    private BigDecimal fats;
    private BigDecimal carbs;
    private List<Ingredient> ingredientsList;
    private List<Tag> tagsList;

    public Integer getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getPortions() {
        return portions;
    }

    public void setPortions(Integer portions) {
        this.portions = portions;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
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

    public List<Ingredient> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(List<Ingredient> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public List<Tag> getTagsList() {
        return tagsList;
    }

    public void setTagsList(List<Tag> tagsList) {
        this.tagsList = tagsList;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", portions=" + portions +
                ", weight=" + weight +
                ", calories=" + calories +
                ", proteins=" + proteins +
                ", fats=" + fats +
                ", carbs=" + carbs +
                ", ingredientsList=" + ingredientsList +
                ", tagsList=" + tagsList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(recipeId, recipe.recipeId)
                && Objects.equals(name, recipe.name)
                && Objects.equals(description, recipe.description)
                && Objects.equals(source, recipe.source)
                && Objects.equals(portions, recipe.portions)
                && Objects.equals(weight, recipe.weight)
                && Objects.equals(calories, recipe.calories)
                && Objects.equals(proteins, recipe.proteins)
                && Objects.equals(fats, recipe.fats)
                && Objects.equals(carbs, recipe.carbs)
                && Objects.equals(ingredientsList, recipe.ingredientsList)
                && Objects.equals(tagsList, recipe.tagsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId, name, description, source, portions, weight, calories, proteins, fats, carbs, ingredientsList, tagsList);
    }
}