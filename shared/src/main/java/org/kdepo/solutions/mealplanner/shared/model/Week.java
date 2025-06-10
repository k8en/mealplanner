package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Week {

    private Integer weekId;
    private Integer menuId;
    private String name;
    private Integer orderNumber;

    public Integer getWeekId() {
        return weekId;
    }

    public void setWeekId(Integer weekId) {
        this.weekId = weekId;
    }

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
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
        return "Week{" +
                "weekId=" + weekId +
                ", menuId=" + menuId +
                ", name='" + name + '\'' +
                ", orderNumber=" + orderNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Week week = (Week) o;
        return Objects.equals(weekId, week.weekId)
                && Objects.equals(menuId, week.menuId)
                && Objects.equals(name, week.name)
                && Objects.equals(orderNumber, week.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weekId, menuId, name, orderNumber);
    }
}
