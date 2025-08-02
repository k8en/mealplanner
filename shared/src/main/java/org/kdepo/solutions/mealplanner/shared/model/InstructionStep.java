package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class InstructionStep {

    private Integer instructionStepId;
    private Integer recipeId;
    private String name;
    private String description;
    private String image;
    private Integer orderNumber;

    public Integer getInstructionStepId() {
        return instructionStepId;
    }

    public void setInstructionStepId(Integer instructionStepId) {
        this.instructionStepId = instructionStepId;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "InstructionStep{" +
                "instructionStepId=" + instructionStepId +
                ", recipeId=" + recipeId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", orderNumber=" + orderNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InstructionStep that = (InstructionStep) o;
        return Objects.equals(instructionStepId, that.instructionStepId)
                && Objects.equals(recipeId, that.recipeId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(image, that.image)
                && Objects.equals(orderNumber, that.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructionStepId, recipeId, name, description, image, orderNumber);
    }
}
