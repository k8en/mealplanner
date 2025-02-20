package org.kdepo.solutions.mealplanner.repository;

public interface MealPlannerPrimaryKeysRepository {

    /**
     * Returns next available primary key value by entity primary key name
     *
     * @return next available primary key value
     */
    Integer getNextVal(String entityPk);

    /**
     * Updates primary key for entity with new value
     */
    void moveNextVal(String entityPk);

}
