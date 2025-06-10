package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Menu {

    private Integer menuId;
    private Integer menuTypeId;
    private String name;
    private Boolean active;

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public Integer getMenuTypeId() {
        return menuTypeId;
    }

    public void setMenuTypeId(Integer menuTypeId) {
        this.menuTypeId = menuTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(menuId, menu.menuId)
                && Objects.equals(menuTypeId, menu.menuTypeId)
                && Objects.equals(name, menu.name)
                && Objects.equals(active, menu.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, menuTypeId, name, active);
    }

    @Override
    public String toString() {
        return "Menu{" +
                "menuId=" + menuId +
                ", menuTypeId=" + menuTypeId +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
