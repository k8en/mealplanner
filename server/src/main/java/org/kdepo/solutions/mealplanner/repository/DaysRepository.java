package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Day;

import java.util.List;

public interface DaysRepository {

    Day addDay(Integer dayId, Integer weekId, String name, Integer orderNumber);

    void deleteDay(Integer dayId);

    List<Day> getAllDaysFromWeek(Integer weekId);

    Day getDay(Integer dayId);

    Integer getOrderNumber(Integer weekId);

    boolean isUsed(Integer dayId);

    void updateDay(Integer dayId, Integer weekId, String name, Integer orderNumber);

}
