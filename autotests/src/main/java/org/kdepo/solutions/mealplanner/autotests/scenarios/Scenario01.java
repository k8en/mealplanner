package org.kdepo.solutions.mealplanner.autotests.scenarios;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;

public class Scenario01 {

    /**
     * <p>Scenario steps (for anonymous user):<p/>
     * <ul>
     * <li>01-1 check access to tags list page</li>
     * <li>01-2 check access to tag details page</li>
     * <li>01-3 check access to tag create form via direct link</li>
     * <li>01-4 check access to tag update form via direct link</li>
     * <li>01-5 check access to tag delete form via direct link</li>
     * <li>01-6 check access to tag-to-recipe set form via direct link</li>
     * <li>01-7 check access to tag-to-recipe unset form via direct link</li>
     * <li>01-8 check access to products list page</li>
     * <li>01-9 check access to product details page</li>
     * <li>01-10 check access to product create form via direct link</li>
     * <li>01-11 check access to product update form via direct link</li>
     * <li>01-12 check access to product delete form via direct link</li>
     * <li>01-13 check access to recipes list page</li>
     * <li>01-14 check access to recipe details page</li>
     * <li>01-15 check access to recipe create form via direct link</li>
     * <li>01-16 check access to recipe update form via direct link</li>
     * <li>01-17 check access to recipe delete form via direct link</li>
     * <li>01-18 check access to recipe tags bulk update via direct link</li>
     * <li>01-19 check access to recipe ingredient details page</li>
     * <li>01-20 check access to ingredient create form via direct link</li>
     * <li>01-21 check access to ingredient update form via direct link</li>
     * <li>01-22 check access to ingredient delete form via direct link</li>
     * <li>01-23 check access to profiles list page</li>
     * </ul>
     *
     * @param robot {@link Robot} auto tester
     */
    public static void execute(Robot robot) {
        System.out.println("[QA] Started scenario 01");

        System.out.println("[QA] 01-1 check access to tags list page");
        robot.openTagsListPage();
        robot.pause(1000);

        System.out.println("[QA] 01-2 check access to tag details page");
        robot.openTagDetailsPage(1);
        robot.pause(1000);

        System.out.println("[QA] 01-3 check access to tag create form via direct link");
        robot.openTagCreationForm();
        robot.pause(1000);

        System.out.println("[QA] 01-4 check access to tag update form via direct link");
        robot.openTagModificationForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-5 check access to tag delete form via direct link");
        robot.openTagDeletionForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-6 check access to tag-to-recipe set form via direct link");
        robot.openTagSetForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-7 check access to tag-to-recipe unset form via direct link");
        robot.openTagUnsetForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-8 check access to products list page");
        robot.openProductsListPage();
        robot.pause(1000);

        System.out.println("[QA] 01-9 check access to product details page");
        robot.openProductDetailsPage(1);
        robot.pause(1000);

        System.out.println("[QA] 01-10 check access to product create form via direct link");
        robot.openProductCreationForm();
        robot.pause(1000);

        System.out.println("[QA] 01-11 check access to product update form via direct link");
        robot.openProductModificationForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-12 check access to product delete form via direct link");
        robot.openProductDeletionForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-13 check access to recipes list page");
        robot.openRecipesListPage();
        robot.pause(1000);

        System.out.println("[QA] 01-14 check access to recipe details page");
        robot.openRecipeDetailsPage(1);
        robot.pause(1000);

        System.out.println("[QA] 01-15 check access to recipe create form via direct link");
        robot.openRecipeCreationForm();
        robot.pause(1000);

        System.out.println("[QA] 01-16 check access to recipe update form via direct link");
        robot.openRecipeModificationForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-17 check access to recipe delete form via direct link");
        robot.openRecipeDeletionForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-18 check access to recipe tags bulk update via direct link");
        robot.openRecipeTagsForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-19 check access to recipe ingredient details page");
        robot.openIngredientDetailsPage(1);
        robot.pause(1000);

        System.out.println("[QA] 01-20 check access to ingredient create form via direct link");
        robot.openIngredientCreationForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-21 check access to ingredient update form via direct link");
        robot.openIngredientModificationForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-22 check access to ingredient delete form via direct link");
        robot.openIngredientDeletionForm(1);
        robot.pause(1000);

        System.out.println("[QA] 01-23 check access to profiles list page");
        robot.openProfilesListPage();
        robot.pause(1000);

        System.out.println("[QA] Completed scenario 01");
    }
}
