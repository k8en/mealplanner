package org.kdepo.solutions.mealplanner.server.dto;

import java.util.List;

public class WeekDto {

    private Integer weekId;
    private String name;
    private List<DayDto> days;

    public Integer getWeekId() {
        return weekId;
    }

    public void setWeekId(Integer weekId) {
        this.weekId = weekId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DayDto> getDays() {
        return days;
    }

    public void setDays(List<DayDto> days) {
        this.days = days;
    }
}
