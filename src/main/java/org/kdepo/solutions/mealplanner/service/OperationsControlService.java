package org.kdepo.solutions.mealplanner.service;

import org.springframework.stereotype.Service;

@Service
public class OperationsControlService {

    public boolean canCreateProduct(String userName) {
        return true;
    }

    public boolean canReadProduct(String userName, Integer productId) {
        return true;
    }

    public boolean canModifyProduct(String userName, Integer productId) {
        return true;
    }

    public boolean canDeleteProduct(String userName, Integer productId) {
        return true;
    }
}
