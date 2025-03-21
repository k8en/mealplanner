package org.kdepo.solutions.mealplanner.autotests;

public class RobotConstants {

    public interface PageTitle {
        String INGREDIENT_CREATE = "Добавление нового ингредиента";
        String INGREDIENT_DELETE = "Удаление информации об ингредиенте";
        String INGREDIENT_UPDATE = "Редактирование информации об ингредиенте";
        String PRODUCT_CREATE = "Добавление нового продукта";
        String PRODUCT_DELETE = "Удаление информации о продукте";
        String PRODUCT_UPDATE = "Редактирование информации о продукте";
        String RECIPE_CREATE = "Добавление нового рецепта";
        String RECIPE_DELETE = "Удаление информации о рецепте";
        String RECIPE_UPDATE = "Редактирование информации о рецепте";
        String TAG_CREATE = "Добавление новой метки";
        String TAG_DELETE = "Удаление информации о метке";
        String TAG_UPDATE = "Редактирование информации о метке";
    }

    private RobotConstants() {
        throw new RuntimeException("Instantiation is not allowed");
    }
}
