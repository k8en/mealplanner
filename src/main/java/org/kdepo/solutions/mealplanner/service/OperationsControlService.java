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

    public boolean canCreateTag(String userName) {
        return true;
    }

    public boolean canReadTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canModifyTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canDeleteTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canSetTag(String userName, Integer tagId) {
        return true;
    }

    public boolean canUnsetTag(String userName, Integer tagId) {
        return true;
    }
}
