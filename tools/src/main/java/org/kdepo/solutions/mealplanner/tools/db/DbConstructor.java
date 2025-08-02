package org.kdepo.solutions.mealplanner.tools.db;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbConstructor {

    private static final String SQL_CREATE_TABLE_PRIMARY_KEYS = ""
            + "CREATE TABLE primary_keys (\n"
            + "    name     VARCHAR (30) UNIQUE\n"
            + "                          NOT NULL,\n"
            + "    next_val NUMERIC (9)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_SETTINGS = ""
            + "CREATE TABLE settings (\n"
            + "    name  VARCHAR (50)  NOT NULL\n"
            + "                        UNIQUE,\n"
            + "    value VARCHAR (250) NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_UNITS = ""
            + "CREATE TABLE units (\n"
            + "    unit_id    NUMERIC (5)  PRIMARY KEY\n"
            + "                            UNIQUE\n"
            + "                            NOT NULL,\n"
            + "    name       VARCHAR (20) NOT NULL,\n"
            + "    short_name VARCHAR (10) NOT NULL,\n"
            + "    accuracy   NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_PRODUCTS = ""
            + "CREATE TABLE products (\n"
            + "    product_id  NUMERIC (5)   PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    name        VARCHAR (50)  NOT NULL,\n"
            + "    description VARCHAR (200),\n"
            + "    calories    NUMERIC (8)   NOT NULL,\n"
            + "    proteins    NUMERIC (8)   NOT NULL,\n"
            + "    fats        NUMERIC (8)   NOT NULL,\n"
            + "    carbs       NUMERIC (8)   NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_TAGS = ""
            + "CREATE TABLE tags (\n"
            + "    tag_id      NUMERIC (5)   PRIMARY KEY\n"
            + "                              NOT NULL\n"
            + "                              UNIQUE,\n"
            + "    name        VARCHAR (50)  NOT NULL\n"
            + "                              UNIQUE,\n"
            + "    description VARCHAR (200) \n"
            + ")";

    private static final String SQL_CREATE_TABLE_INSTRUCTION_TYPES = ""
            + "CREATE TABLE instruction_types (\n"
            + "    instruction_type_id NUMERIC (1)  PRIMARY KEY\n"
            + "                                     UNIQUE\n"
            + "                                     NOT NULL,\n"
            + "    name                VARCHAR (50) NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_RECIPES = ""
            + "CREATE TABLE recipes (\n"
            + "    recipe_id           NUMERIC (5)    PRIMARY KEY\n"
            + "                                       UNIQUE\n"
            + "                                       NOT NULL,\n"
            + "    instruction_type_id NUMERIC (1)    NOT NULL\n"
            + "                                       REFERENCES instruction_types (instruction_type_id),\n"
            + "    name                VARCHAR (200)  NOT NULL,\n"
            + "    description         VARCHAR (2000) NOT NULL,\n"
            + "    source              VARCHAR (200),\n"
            + "    portions            NUMERIC (2)    NOT NULL,\n"
            + "    weight              NUMERIC (8)    NOT NULL,\n"
            + "    calories            NUMERIC (8)    NOT NULL,\n"
            + "    proteins            NUMERIC (8)    NOT NULL,\n"
            + "    fats                NUMERIC (8)    NOT NULL,\n"
            + "    carbs               NUMERIC (8)    NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_RECIPES_TAGS = ""
            + "CREATE TABLE recipes_tags (\n"
            + "    recipe_id NUMERIC (5) NOT NULL\n"
            + "                          REFERENCES recipes (recipe_id),\n"
            + "    tag_id    NUMERIC (5) NOT NULL\n"
            + "                          REFERENCES tags (tag_id) \n"
            + ")";

    private static final String SQL_CREATE_TABLE_INGREDIENTS = ""
            + "CREATE TABLE ingredients (\n"
            + "    ingredient_id NUMERIC (5)  PRIMARY KEY\n"
            + "                               NOT NULL\n"
            + "                               UNIQUE,\n"
            + "    name          VARCHAR (50) NOT NULL,\n"
            + "    recipe_id     NUMERIC (5)  NOT NULL\n"
            + "                               REFERENCES recipes (recipe_id),\n"
            + "    product_id    NUMERIC (5)  NOT NULL\n"
            + "                               REFERENCES products (product_id),\n"
            + "    amount        NUMERIC (6)  NOT NULL,\n"
            + "    unit_id       NUMERIC (5)  NOT NULL\n"
            + "                               REFERENCES units (unit_id) \n"
            + ")";

    private static final String SQL_CREATE_TABLE_INSTRUCTIONS_STEPS = ""
            + "CREATE TABLE instructions_steps (\n"
            + "    instruction__step_id NUMERIC (5) PRIMARY KEY\n"
            + "                         NOT NULL\n"
            + "                         UNIQUE,\n"
            + "    recipe_id            NUMERIC (5) NOT NULL\n"
            + "                         REFERENCES recipes (recipe_id),\n"
            + "    name                 VARCHAR (50) NOT NULL,\n"
            + "    description          VARCHAR (2000) NOT NULL,\n"
            + "    image                VARCHAR (250),\n"
            + "    order_number         NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_MENU_TYPES = ""
            + "CREATE TABLE menu_types (\n"
            + "    menu_type_id NUMERIC (1)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    name         VARCHAR (20) NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_MENUS = ""
            + "CREATE TABLE menus (\n"
            + "    menu_id      NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    menu_type_id NUMERIC (1)  NOT NULL\n"
            + "                              REFERENCES menu_types (menu_type_id),\n"
            + "    name         VARCHAR (50) NOT NULL,\n"
            + "    active       NUMERIC (1)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_WEEKS = ""
            + "CREATE TABLE weeks (\n"
            + "    week_id      NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    menu_id      NUMERIC (5)  NOT NULL\n"
            + "                              REFERENCES menus (menu_id),\n"
            + "    name         VARCHAR (20) NOT NULL,\n"
            + "    order_number NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_DAYS = ""
            + "CREATE TABLE days (\n"
            + "    day_id       NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    menu_id      NUMERIC (5)  NOT NULL\n"
            + "                              REFERENCES menus (menu_id),\n"
            + "    week_id      NUMERIC (5)  REFERENCES weeks (week_id),\n"
            + "    name         VARCHAR (20) NOT NULL,\n"
            + "    order_number NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_MEALS = ""
            + "CREATE TABLE meals (\n"
            + "    meal_id      NUMERIC (5)  PRIMARY KEY\n"
            + "                              NOT NULL\n"
            + "                              UNIQUE,\n"
            + "    day_id       NUMERIC (5)  NOT NULL\n"
            + "                              REFERENCES days (day_id),\n"
            + "    name         VARCHAR (20) NOT NULL,\n"
            + "    order_number NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_MEALS_CONTENTS = ""
            + "CREATE TABLE meals_contents (\n"
            + "    meal_id      NUMERIC (5) NOT NULL\n"
            + "                             REFERENCES meals (meal_id),\n"
            + "    recipe_id    NUMERIC (5) NOT NULL\n"
            + "                             REFERENCES recipes (recipe_id),\n"
            + "    order_number NUMERIC (5) NOT NULL\n"
            + ")";

    private static final List<String> SQL_INSERT_DATA_LINES = Arrays.asList(
            "INSERT INTO primary_keys (name, next_val) VALUES ('day_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('ingredient_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('instruction_step_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('meal_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('menu_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('product_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('recipe_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('tag_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('unit_id', 1)"
            , "INSERT INTO primary_keys (name, next_val) VALUES ('week_id', 1)"

            , "INSERT INTO menu_types (menu_type_id, name) VALUES ('1', 'Группировка по дням')"
            , "INSERT INTO menu_types (menu_type_id, name) VALUES ('2', 'Группировка по неделям')"

            , "INSERT INTO instruction_types (instruction_type_id, name) VALUES ('1', 'Простая текстовая инструкция')"
            , "INSERT INTO instruction_types (instruction_type_id, name) VALUES ('2', 'Пошаговая инструкция с картинками')"

            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('1', 'Штука', 'шт', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('2', 'Грамм', 'г', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('3', 'Милилитр', 'мл', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('4', 'Столовая ложка', 'ст.л', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('5', 'Чайная ложка', 'ч.л', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('6', 'По вкусу', 'по вкусу', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('7', 'Щепотка', 'щепотка', '0')"
            , "UPDATE primary_keys  SET next_val = '8' WHERE name = 'unit_id'"
    );

    private static final List<String> SQL_INSERT_TEST_LINES = Arrays.asList(
            "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (1, 'Вода', null, 0, 0, 0, 0)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (2, 'Гречка', null, 3130000, 126000, 33000, 621000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (3, 'Картофель', null, 800000, 20000, 4000, 181000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (4, 'Крабовые палочки', null, 730000, 60000, 10000, 100000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (5, 'Кукуруза консервированная', null, 790000, 24100, 5000, 174400)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (6, 'Масло сливочное', null, 7340000, 5000, 825000, 8000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (7, 'Морковь', null, 330000, 13000, 1000, 69000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (8, 'Огурец', null, 150000, 8000, 1000, 26000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (9, 'Яйцо куриное', null, 1570000, 127000, 109000, 7000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (10, 'Масло растительное', null, 8730000, 0, 999000, 0)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (11, 'Майонез', null, 6240000, 31000, 670000, 26000)"
            , "INSERT INTO products (product_id, name, description, calories, proteins, fats, carbs) VALUES (12, 'Рис', null, 3440000, 67000, 7000, 789000)"
            , "UPDATE primary_keys  SET next_val = '13' WHERE name = 'product_id'"

            , "INSERT INTO tags (tag_id, name, description) VALUES (1, 'Завтрак', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (2, 'Перекус', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (3, 'Обед', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (4, 'Полдник', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (5, 'Ужин', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (6, 'Напиток безалкогольный', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (7, 'Напиток алкогольный', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (8, 'Каша', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (9, 'Суп', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (10, 'Салат', null)"
            , "INSERT INTO tags (tag_id, name, description) VALUES (11, 'Торт', null)"
            , "UPDATE primary_keys  SET next_val = '12' WHERE name = 'tag_id'"
    );

    private static DbConstructor instance;

    public static DbConstructor getInstance() {
        if (instance == null) {
            instance = new DbConstructor();
        }
        return instance;
    }

    private String url;

    private DbConstructor() {

    }

    public boolean connect(String pathToDatabase) {
        Connection connection = null;
        try {
            url = "jdbc:sqlite:" + pathToDatabase + "meal_planner_empty.db";
            System.out.println("[DB] Create database for [" + url + "]");
            connection = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.out.println("[DB] No connection for [" + url + "]");
            e.printStackTrace();
            return false;

        } finally {
            DbUtils.closeQuietly(connection);
        }

        return true;
    }

    public boolean construct(boolean isDataLinesRequired, boolean isTestLinesRequired) {
        List<String> createTableQueryList = new ArrayList<>();
        createTableQueryList.add(SQL_CREATE_TABLE_PRIMARY_KEYS);
        createTableQueryList.add(SQL_CREATE_TABLE_SETTINGS);
        createTableQueryList.add(SQL_CREATE_TABLE_UNITS);
        createTableQueryList.add(SQL_CREATE_TABLE_PRODUCTS);
        createTableQueryList.add(SQL_CREATE_TABLE_TAGS);
        createTableQueryList.add(SQL_CREATE_TABLE_INSTRUCTION_TYPES);
        createTableQueryList.add(SQL_CREATE_TABLE_RECIPES);
        createTableQueryList.add(SQL_CREATE_TABLE_RECIPES_TAGS);
        createTableQueryList.add(SQL_CREATE_TABLE_INGREDIENTS);
        createTableQueryList.add(SQL_CREATE_TABLE_INSTRUCTIONS_STEPS);
        createTableQueryList.add(SQL_CREATE_TABLE_MENU_TYPES);
        createTableQueryList.add(SQL_CREATE_TABLE_MENUS);
        createTableQueryList.add(SQL_CREATE_TABLE_WEEKS);
        createTableQueryList.add(SQL_CREATE_TABLE_DAYS);
        createTableQueryList.add(SQL_CREATE_TABLE_MEALS);
        createTableQueryList.add(SQL_CREATE_TABLE_MEALS_CONTENTS);

        for (String query : createTableQueryList) {
            if (!execute(query)) {
                return false;
            }
        }

        if (isDataLinesRequired) {
            for (String query : SQL_INSERT_DATA_LINES) {
                if (!execute(query)) {
                    return false;
                }
            }
        }

        if (isTestLinesRequired) {
            for (String query : SQL_INSERT_TEST_LINES) {
                if (!execute(query)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean execute(String query) {
        Connection connection = null;
        Statement statement = null;

        boolean success = true;

        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setEncoding(SQLiteConfig.Encoding.UTF8);
            connection = DriverManager.getConnection(url, config.toProperties());
            statement = connection.createStatement();
            statement.execute(query);

        } catch (Exception e) {
            success = false;
            System.out.println("[DB] Query failed:");
            System.out.println(query);
            e.printStackTrace();

        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return success;
    }
}
