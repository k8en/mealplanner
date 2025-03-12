package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Week;

import java.util.List;

public interface WeeksRepository {

    Week addWeek(Integer weekId, Integer profileId, String name, Integer orderNumber);

    void deleteWeek(Integer weekId);

    List<Week> getAllWeeksFromProfile(Integer profileId);

    Integer getOrderNumber(Integer profileId);

    Week getWeek(Integer weekId);

    boolean isUsed(Integer weekId);

    void updateWeek(Integer weekId, Integer profileId, String name, Integer orderNumber);

}
