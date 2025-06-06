package org.kdepo.solutions.mealplanner.autotests.scenarios;

import org.kdepo.solutions.mealplanner.autotests.robot.Robot;
import org.kdepo.solutions.mealplanner.shared.model.Product;
import org.kdepo.solutions.mealplanner.shared.model.Tag;

import java.math.BigDecimal;
import java.util.UUID;

public class Scenario02 {

    public static void execute(Robot robot, String username, String password) {
        System.out.println("[QA] Started scenario 02");

        // Generate unique key
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("[QA] Test key is generated as " + uuid);
        System.out.println();

        // Login as user
        robot.login(username, password);
        robot.pause(1000);

        // Check list pages as user
        robot.openTagsListPage();
        robot.pause(1000);
        robot.openProductsListPage();
        robot.pause(1000);
        robot.openRecipesListPage();
        robot.pause(1000);

        // Tag manipulation
        // 1. Create new tag
        Tag tag = new Tag();
        tag.setName("Метка_" + uuid);
        tag.setDescription("Описание_" + uuid);
        robot.openTagCreationForm();
        robot.pause(1000);
        robot.fillTagCreationForm(tag.getName(), tag.getDescription());
        robot.pause(1000);
        Integer tagId = robot.getTagIdFromUrl();
        tag.setTagId(tagId);

        // 2. Check that tag is created as expected
        robot.compareTag(tag.getTagId(), tag.getName(), tag.getDescription());
        robot.pause(1000);

        // 3. Update tag
        tag.setName(tag.getName() + "_edited");
        tag.setDescription(tag.getDescription() + "_edited");
        robot.openTagModificationForm(tag.getTagId());
        robot.pause(1000);
        robot.fillTagModificationForm(tag.getTagId(), tag.getName(), tag.getDescription());
        robot.pause(1000);

        // 4. Check that tag is updated as expected
        robot.compareTag(tag.getTagId(), tag.getName(), tag.getDescription());
        robot.pause(1000);

        // 5. Delete tag
        robot.openTagDeletionForm(tag.getTagId());
        robot.pause(1000);
        robot.submitForm();

        // 6. Check that tag is deleted
        //

        System.out.println();

        // Product manipulation
        // 1. Create new product
        Product product = new Product();
        product.setName("Продукт_" + uuid);
        product.setDescription("Описание_" + uuid);
        product.setCalories(BigDecimal.valueOf(10L));
        product.setProteins(BigDecimal.valueOf(20L));
        product.setFats(BigDecimal.valueOf(30L));
        product.setCarbs(BigDecimal.valueOf(40L));
        robot.openProductCreationForm();
        robot.pause(1000);
        robot.createProduct(product.getName(), product.getDescription(), product.getCalories(), product.getProteins(), product.getFats(), product.getCarbs());
        robot.pause(1000);
        Integer productId = robot.getProductIdFromUrl();
        product.setProductId(productId);

        // 2. Check that product is created as expected
        robot.compareProduct(product.getProductId(), product.getName(), product.getDescription(), product.getCalories(), product.getProteins(), product.getFats(), product.getCarbs());
        robot.pause(1000);

        // 3. Update product
        product.setName(product.getName() + "_edited");
        product.setDescription(product.getDescription() + "_edited");
        product.setCalories(product.getCalories().add(BigDecimal.ONE));
        product.setProteins(product.getProteins().add(BigDecimal.ONE));
        product.setFats(product.getFats().add(BigDecimal.ONE));
        product.setCarbs(product.getCarbs().add(BigDecimal.ONE));
        robot.updateProduct(product.getProductId(), product.getName(), product.getDescription(), product.getCalories(), product.getProteins(), product.getFats(), product.getCarbs());
        robot.pause(1000);

        // 4. Check that product is updated as expected
        robot.compareProduct(product.getProductId(), product.getName(), product.getDescription(), product.getCalories(), product.getProteins(), product.getFats(), product.getCarbs());
        robot.pause(1000);

        // 5. Delete product
        robot.deleteProduct(product.getProductId());

        // Logout
        robot.logout();

        System.out.println("[QA] Completed scenario 02");
    }
}
