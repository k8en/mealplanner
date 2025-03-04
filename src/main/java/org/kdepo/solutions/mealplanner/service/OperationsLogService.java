package org.kdepo.solutions.mealplanner.service;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Product;
import org.springframework.stereotype.Service;

@Service
public class OperationsLogService {

    public void registerProductCreated(String userName, Product createdProduct) {

    }

    public void registerProductUpdated(String userName, Product oldProductData, @Valid Product newProductData) {

    }

    public void registerProductDeleted(String userName, Integer productId) {

    }
}
