package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Week;

import java.util.List;

public interface WeeksRepository {

    Week addWeek(Integer weekId, Integer menuId, String name, Integer orderNumber);

    void deleteWeek(Integer weekId);

    List<Week> getAllWeeksFromMenu(Integer menuId);

    Integer getOrderNumber(Integer menuId);

    Week getWeek(Integer weekId);

    boolean isUsed(Integer weekId);

    void updateWeek(Integer weekId, Integer menuId, String name, Integer orderNumber);

}
