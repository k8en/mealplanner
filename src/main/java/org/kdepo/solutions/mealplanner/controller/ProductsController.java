package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.repository.MealPlannerProductsRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerRecipesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private MealPlannerProductsRepository productsRepository;

    @Autowired
    private MealPlannerRecipesRepository recipesRepository;

    @GetMapping
    public String showProductsListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/products");

        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        return "products_list";
    }

    @GetMapping("/{pid}")
    public String showDeviceDetailsPage(@PathVariable Integer pid, Model model) {
        System.out.println("[WEB]" + " GET " + "/products/" + pid);

        Product product = productsRepository.getProduct(pid);
        if (product != null) {
            model.addAttribute("product", product);

            List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.singletonList(product.getProductId()), Collections.emptyList());
            model.addAttribute("recipes", recipes);

            return "product_details";
        } else {
            return "redirect:/products_list";
        }
    }

}
