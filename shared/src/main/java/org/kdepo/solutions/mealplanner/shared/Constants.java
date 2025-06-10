package org.kdepo.solutions.mealplanner.shared;

public class Constants {

    public interface MenuType {
        Integer UNDEFINED = 0;
        Integer DAYS_WITHOUT_GROUPING = 1;
        Integer DAYS_GROUPED_BY_WEEKS = 2;
    }

    private Constants() {
        throw new RuntimeException("Instantiation is not allowed");
    }
}