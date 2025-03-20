package org.kdepo.solutions.mealplanner.autotests;

public class RobotConstants {

    public interface PageTitle {
        String PRODUCT_CREATE = "Добавление нового продукта";
        String PRODUCT_UPDATE = "Редактирование информации о продукте";
        String TAG_CREATE = "Добавление новой метки";
        String TAG_UPDATE = "Редактирование информации о метке";
    }

    private RobotConstants() {
        throw new RuntimeException("Instantiation is not allowed");
    }
}
