package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Menu;

import java.util.List;

public interface MenusRepository {

    Menu addMenu(Integer menuId, Integer menuTypeId, String name, Boolean active);

    void deleteMenu(Integer menuId);

    List<Menu> getAllMenus();

    Menu getMenu(Integer menuId);

    boolean isUsed(Integer menuId);

    void updateMenu(Integer menuId, Integer menuTypeId, String name, Boolean active);

}
