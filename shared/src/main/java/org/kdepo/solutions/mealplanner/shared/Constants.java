package org.kdepo.solutions.mealplanner.shared;

public class Constants {

    public interface InstructionType {
        Integer UNDEFINED = 0;
        Integer PLAIN_TEXT = 1;
        Integer STEP_BY_STEP = 2;
    }

    public interface MenuType {
        Integer UNDEFINED = 0;
        Integer DAYS_WITHOUT_GROUPING = 1;
        Integer DAYS_GROUPED_BY_WEEKS = 2;
    }

    private Constants() {
        throw new RuntimeException("Instantiation is not allowed");
    }
}