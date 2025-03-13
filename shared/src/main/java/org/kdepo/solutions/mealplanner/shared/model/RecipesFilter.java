package org.kdepo.solutions.mealplanner.shared.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecipesFilter {

    private List<Integer> selectedProducts;
    private List<Integer> selectedTags;

    public RecipesFilter() {
        selectedProducts = new ArrayList<>();
        selectedTags = new ArrayList<>();
    }

    public List<Integer> getSelectedProducts() {
        return selectedProducts;
    }

    public void setSelectedProducts(List<Integer> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    public List<Integer> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<Integer> selectedTags) {
        this.selectedTags = selectedTags;
    }

    @Override
    public String toString() {
        return "RecipesFilter{" +
                "selectedProducts=" + selectedProducts +
                ", selectedTags=" + selectedTags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipesFilter that = (RecipesFilter) o;
        return Objects.equals(selectedProducts, that.selectedProducts)
                && Objects.equals(selectedTags, that.selectedTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedProducts, selectedTags);
    }
}
