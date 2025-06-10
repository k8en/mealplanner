package org.kdepo.solutions.mealplanner.server.dto;

import java.util.List;

public class MenuDto {

    private Integer menuId;
    private String name;
    private List<WeekDto> weeks;

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

    public List<WeekDto> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<WeekDto> weeks) {
        this.weeks = weeks;
    }
}
