package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Product;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.ProductsRepository;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.service.OperationsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    private static final String PK = "product_id";

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping
    public String showProductsListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/products");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Prepare entities
        List<Product> products = productsRepository.getAllProducts();
        model.addAttribute("products", products);

        return "products_list";
    }

    @GetMapping("/{id}")
    public String showProductDetailsPage(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/products/" + id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Product product = productsRepository.getProduct(id);
        if (product == null) {
            System.out.println("Redirect to products list: product not found " + id);
            return "redirect:/products";
        }

        if (!controlService.canReadProduct(userName, product.getProductId())) {
            System.out.println("Redirect to products list: user '" + userName + "' cannot read product info");
            return "redirect:/products";
        }

        // Prepare entities
        model.addAttribute("product", product);

        List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.singletonList(product.getProductId()), Collections.emptyList());
        model.addAttribute("recipes", recipes);

        return "product_details";
    }

    @GetMapping("/create")
    public String showProductCreationForm(Model model) {
        System.out.println("[WEB]" + " GET " + "/products/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect to products list: anonymous user cannot create products");
            return "redirect:/products";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateProduct(userName)) {
            System.out.println("Redirect to products list: user '" + userName + "' cannot create products");
            return "redirect:/products";
        }

        // Prepare entity with default values
        Product product = new Product();
        product.setProductId(-1);
        product.setCalories(BigDecimal.ZERO);
        product.setProteins(BigDecimal.ZERO);
        product.setFats(BigDecimal.ZERO);
        product.setCarbs(BigDecimal.ZERO);

        model.addAttribute("product", product);

        return "product_create";
    }

    @PostMapping("/create")
    public String acceptProductCreationForm(@Valid Product product, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/products/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect to products list: anonymous user cannot create products");
            return "redirect:/products";
        }

        // Operation availability checks
        if (!controlService.canCreateProduct(userName)) {
            System.out.println("Redirect to products list: user '" + userName + "' cannot create products");
            return "redirect:/products";
        }

        // Validate that provided data is correct
        String productName = product.getName();
        if (productName == null || productName.isEmpty()) {
            FieldError fieldError = new FieldError("product", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "product_create";
        }

        if (productName.length() > 50) {
            FieldError fieldError = new FieldError("product", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "product_create";
        }

        List<Product> allProducts = productsRepository.getAllProducts();
        for (Product productToValidate : allProducts) {
            if (productToValidate.getName().equalsIgnoreCase(productName)) {
                FieldError fieldError = new FieldError("product", "name", "Объект с таким именем уже существует!");
                result.addError(fieldError);
                return "product_create";
            }
        }

        if (BigDecimal.ZERO.compareTo(product.getCalories()) > 0) {
            FieldError fieldError = new FieldError("product", "calories", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "product_create";
        }

        if (BigDecimal.ZERO.compareTo(product.getProteins()) > 0) {
            FieldError fieldError = new FieldError("product", "proteins", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "product_create";
        }

        if (BigDecimal.ZERO.compareTo(product.getFats()) > 0) {
            FieldError fieldError = new FieldError("product", "fats", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "product_create";
        }

        if (BigDecimal.ZERO.compareTo(product.getCarbs()) > 0) {
            FieldError fieldError = new FieldError("product", "carbs", "Значение не может быть отрицательным!");
            result.addError(fieldError);
            return "product_create";
        }

        if (product.getDescription() != null && product.getDescription().length() > 200) {
            FieldError fieldError = new FieldError("product", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "product_create";
        }

        // Generate primary key for new entity
        Integer productId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        product.setProductId(productId);

        // Create entity
        Product createdProduct = productsRepository.addProduct(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getCalories(),
                product.getProteins(),
                product.getFats(),
                product.getCarbs()
        );

        // Register operation in system events log
        logService.registerProductCreated(userName, createdProduct);

        return "redirect:/products/" + product.getProductId();
    }

    @GetMapping("/{id}/update")
    public String showProductModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/products/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to product: anonymous user cannot modify products");
            return "redirect:/products/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Product product = productsRepository.getProduct(id);
        if (product == null) {
            System.out.println("Redirect to products list: product not found " + id);
            return "redirect:/products";
        }

        if (!controlService.canModifyProduct(userName, product.getProductId())) {
            System.out.println("Redirect back to product: user '" + userName + "' cannot modify products");
            return "redirect:/products/" + product.getProductId();
        }

        model.addAttribute("product", product);

        return "product_update";
    }

    @PostMapping("/{id}/update")
    public String acceptProductModificationForm(@Valid Product product, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/products/" + id + "/update");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to product: anonymous user cannot modify products");
            return "redirect:/products/" + id;
        }

        Product productFromDb = productsRepository.getProduct(product.getProductId());
        if (productFromDb == null) {
            System.out.println("Redirect to products list: product not found " + id);
            return "redirect:/products";
        }

        if (!controlService.canModifyProduct(userName, productFromDb.getProductId())) {
            System.out.println("Redirect back to product: user '" + userName + "' cannot modify products");
            return "redirect:/products/" + productFromDb.getProductId();
        }

        // Validate that provided data is correct
        String productName = product.getName();
        if (productName == null || productName.isEmpty()) {
            FieldError nameFieldError = new FieldError("product", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "product_update";
        }

        if (productName.length() > 50) {
            FieldError nameFieldError = new FieldError("product", "name", "Название не может быть длиннее 50 символов!");
            result.addError(nameFieldError);
            return "product_update";
        }

        List<Product> allProducts = productsRepository.getAllProducts();
        for (Product productToValidate : allProducts) {
            if (productToValidate.getName().equalsIgnoreCase(productName)
                    && !productToValidate.getProductId().equals(id)) {
                FieldError nameFieldError = new FieldError("product", "name", "Объект с таким именем уже существует!");
                result.addError(nameFieldError);
                return "product_update";
            }
        }

        if (BigDecimal.ZERO.compareTo(product.getCalories()) > 0) {
            FieldError nameFieldError = new FieldError("product", "calories", "Значение не может быть отрицательным!");
            result.addError(nameFieldError);
            return "product_update";
        }

        if (BigDecimal.ZERO.compareTo(product.getProteins()) > 0) {
            FieldError nameFieldError = new FieldError("product", "proteins", "Значение не может быть отрицательным!");
            result.addError(nameFieldError);
            return "product_update";
        }

        if (BigDecimal.ZERO.compareTo(product.getFats()) > 0) {
            FieldError nameFieldError = new FieldError("product", "fats", "Значение не может быть отрицательным!");
            result.addError(nameFieldError);
            return "product_update";
        }

        if (BigDecimal.ZERO.compareTo(product.getCarbs()) > 0) {
            FieldError nameFieldError = new FieldError("product", "carbs", "Значение не может быть отрицательным!");
            result.addError(nameFieldError);
            return "product_update";
        }

        if (product.getDescription() != null && product.getDescription().length() > 200) {
            FieldError nameFieldError = new FieldError("product", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(nameFieldError);
            return "product_update";
        }

        // Update entity
        productsRepository.updateProduct(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getCalories(),
                product.getProteins(),
                product.getFats(),
                product.getCarbs()
        );

        // Register operation in system events log
        logService.registerProductUpdated(userName, productFromDb, product);

        return "redirect:/products/" + product.getProductId();
    }

    @GetMapping("/{id}/delete")
    public String showProductDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/products/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            System.out.println("Redirect back to product: anonymous user cannot delete products");
            return "redirect:/products/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Product product = productsRepository.getProduct(id);
        if (product == null) {
            System.out.println("Redirect to products list: product not found " + id);
            return "redirect:/products";
        }

        if (!controlService.canDeleteProduct(userName, product.getProductId())) {
            System.out.println("Redirect back to product: user '" + userName + "' cannot delete products");
            return "redirect:/products/" + product.getProductId();
        }

        model.addAttribute("product", product);

        return "product_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptProductDeletionForm(@Valid Product product, @PathVariable Integer id, RedirectAttributes redirectAttributes) {
        System.out.println("[WEB]" + " POST " + "/products/" + id + "/delete");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            System.out.println("Redirect back to product: anonymous user cannot delete products");
            return "redirect:/products/" + id;
        }

        Product productFromDb = productsRepository.getProduct(id);
        if (productFromDb == null) {
            System.out.println("Redirect to products list: product not found " + id);
            return "redirect:/products";
        }

        if (!controlService.canDeleteProduct(userName, productFromDb.getProductId())) {
            System.out.println("Redirect back to product: user '" + userName + "' cannot delete products");
            return "redirect:/products/" + productFromDb.getProductId();
        }

        // Validate usages in ingredients
        if (productsRepository.isUsed(productFromDb.getProductId())) {
            redirectAttributes.addFlashAttribute("title", "Операция не может быть выполнена!");
            redirectAttributes.addFlashAttribute("details", "Продукт используется в ингредиентах. Сначала удалите ингредиенты.");
            System.out.println("Business error: product " + productFromDb.getProductId() + " cannot be deleted");
            return "redirect:/business_error";
        }

        // Delete entity
        productsRepository.deleteProduct(productFromDb.getProductId());

        // Register operation in system events log
        logService.registerProductDeleted(userName, productFromDb.getProductId());

        return "redirect:/products";
    }
}
