package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Meal {

    private Integer mealId;
    private Integer dayId;
    private String name;
    private Integer orderNumber;

    public Integer getMealId() {
        return mealId;
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "mealId=" + mealId +
                ", dayId=" + dayId +
                ", name='" + name + '\'' +
                ", order=" + orderNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return Objects.equals(mealId, meal.mealId)
                && Objects.equals(dayId, meal.dayId)
                && Objects.equals(name, meal.name)
                && Objects.equals(orderNumber, meal.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealId, dayId, name, orderNumber);
    }
}
