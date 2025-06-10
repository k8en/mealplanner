package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Menu;
import org.kdepo.solutions.mealplanner.shared.repository.MenusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MenusRepositoryImpl implements MenusRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenusRepositoryImpl.class);

    private static final String SQL_ADD_MENU = "INSERT INTO menus (menu_id, menu_type_id, name, active) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_MENU = "DELETE FROM menus WHERE menu_id = ?";
    private static final String SQL_GET_ALL_MENUS = "SELECT * FROM menus ORDER BY active DESC, name ASC";
    private static final String SQL_GET_MENU = "SELECT * FROM menus WHERE menu_id = ?";
    private static final String SQL_IS_USED = "SELECT menu_id FROM weeks WHERE menu_id = ? LIMIT 1";
    private static final String SQL_UPDATE_MENU = "UPDATE menus SET menu_type_id = ?, name = ?, active = ? WHERE menu_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MenusRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Menu addMenu(Integer menuId, Integer menuTypeId, String name, Boolean active) {
        LOGGER.trace("[DBR][addMenu] Invoked with parameters: menuId={}, menuTypeId={}, name={}, active={}",
                menuId, menuTypeId, name, active
        );

        int activeAsInt = active ? 1 : 0;

        jdbcTemplate.update(
                SQL_ADD_MENU,
                ps -> {
                    ps.setInt(1, menuId);
                    ps.setInt(2, menuTypeId);
                    ps.setString(3, name);
                    ps.setInt(4, activeAsInt);
                }
        );

        return getMenu(menuId);
    }

    @Override
    public void deleteMenu(Integer menuId) {
        LOGGER.trace("[DBR][deleteMenu] Invoked with parameters: menuId={}", menuId);

        jdbcTemplate.update(
                SQL_DELETE_MENU,
                ps -> ps.setInt(1, menuId)
        );
    }

    @Override
    public List<Menu> getAllMenus() {
        LOGGER.trace("[DBR][getAllMenus] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_MENUS,
                rs -> {
                    List<Menu> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Menu getMenu(Integer menuId) {
        LOGGER.trace("[DBR][getMenu] Invoked with parameters: menuId={}", menuId);
        return jdbcTemplate.query(
                SQL_GET_MENU,
                ps -> ps.setInt(1, menuId),
                rs -> {
                    Menu menu = null;
                    if (rs.next()) {
                        menu = convert(rs);
                    }
                    return menu;
                }
        );
    }

    @Override
    public boolean isUsed(Integer menuId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: menuId={}", menuId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, menuId),
                ResultSet::next
        ));
    }

    @Override
    public void updateMenu(Integer menuId, Integer menuTypeId, String name, Boolean active) {
        LOGGER.trace("[DBR][updateMenu] Invoked with parameters: menuId={}, menuTypeId={}, name={}, active={}",
                menuId, menuTypeId, name, active
        );

        int activeAsInt = active ? 1 : 0;

        jdbcTemplate.update(
                SQL_UPDATE_MENU,
                ps -> {
                    ps.setInt(1, menuTypeId);
                    ps.setString(2, name);
                    ps.setInt(3, activeAsInt);
                    ps.setInt(4, menuId);
                }
        );
    }

    private Menu convert(ResultSet rs) throws SQLException {
        Integer menuId = rs.getInt("menu_id");
        Integer menuTypeId = rs.getInt("menu_type_id");
        String name = rs.getString("name");
        Integer activeAsInt = rs.getInt("active");

        Menu menu = new Menu();
        menu.setMenuId(menuId);
        menu.setMenuTypeId(menuTypeId);
        menu.setName(name);
        menu.setActive(activeAsInt != 0);

        return menu;
    }
}
