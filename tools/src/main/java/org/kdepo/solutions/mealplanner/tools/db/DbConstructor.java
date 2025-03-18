package org.kdepo.solutions.mealplanner.tools.db;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    private static final String SQL_CREATE_TABLE_RECIPES = ""
            + "CREATE TABLE recipes (\n"
            + "    recipe_id   NUMERIC (5)    PRIMARY KEY\n"
            + "                               UNIQUE\n"
            + "                               NOT NULL,\n"
            + "    name        VARCHAR (200)  NOT NULL,\n"
            + "    description VARCHAR (2000) NOT NULL,\n"
            + "    source      VARCHAR (200),\n"
            + "    portions    NUMERIC (2)    NOT NULL,\n"
            + "    weight      NUMERIC (8)    NOT NULL,\n"
            + "    calories    NUMERIC (8)    NOT NULL,\n"
            + "    proteins    NUMERIC (8)    NOT NULL,\n"
            + "    fats        NUMERIC (8)    NOT NULL,\n"
            + "    carbs       NUMERIC (8)    NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_RECIPES_TAGS = ""
            + "CREATE TABLE recipes_tags (\n"
            + "    recipe_id NUMERIC (5) NOT NULL\n"
            + "                          REFERENCES recipes (recipe_id),\n"
            + "    tag_id    NUMERIC (5) NOT NULL\n"
            + "                          REFERENCES tags (tag_id) \n"
            + ")";

    private static final String SQL_CREATE_TABLE_INGREDIENTS = "" +
            "CREATE TABLE ingredients (\n" +
            "    ingredient_id NUMERIC (5)  PRIMARY KEY\n" +
            "                               NOT NULL\n" +
            "                               UNIQUE,\n" +
            "    name          VARCHAR (50) NOT NULL,\n" +
            "    recipe_id     NUMERIC (5)  NOT NULL\n" +
            "                               REFERENCES recipes (recipe_id),\n" +
            "    product_id    NUMERIC (5)  NOT NULL\n" +
            "                               REFERENCES products (product_id),\n" +
            "    amount        NUMERIC (6)  NOT NULL,\n" +
            "    unit_id       NUMERIC (5)  NOT NULL\n" +
            "                               REFERENCES units (unit_id) \n" +
            ")";

    private static final String SQL_CREATE_TABLE_PROFILES = ""
            + "CREATE TABLE profiles (\n"
            + "    profile_id   NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    name         VARCHAR (50) NOT NULL,\n"
            + "    order_number NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_WEEKS = ""
            + "CREATE TABLE weeks (\n"
            + "    week_id      NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    profile_id   NUMERIC (5)  NOT NULL\n"
            + "                              REFERENCES profiles (profile_id),\n"
            + "    name         VARCHAR (20) NOT NULL,\n"
            + "    order_number NUMERIC (5)  NOT NULL\n"
            + ")";

    private static final String SQL_CREATE_TABLE_DAYS = ""
            + "CREATE TABLE days (\n"
            + "    day_id       NUMERIC (5)  PRIMARY KEY\n"
            + "                              UNIQUE\n"
            + "                              NOT NULL,\n"
            + "    week_id      NUMERIC (5)  NOT NULL\n"
            + "                              REFERENCES weeks (week_id),\n"
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
            "INSERT INTO primary_keys (name, next_val) VALUES ('day_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('ingredient_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('meal_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('product_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('recipe_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('tag_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('unit_id', 1)",
            "INSERT INTO primary_keys (name, next_val) VALUES ('week_id', 1)"

            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('1', 'Штука', 'шт', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('2', 'Грамм', 'г', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('3', 'Милилитр', 'мл', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('4', 'Столовая ложка', 'ст.л', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('5', 'Чайная ложка', 'ч.л', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('6', 'По вкусу', 'по вкусу', '0')"
            , "INSERT INTO units (unit_id, name, short_name, accuracy) VALUES ('7', 'Щепотка', 'щепотка', '0')"
            , "UPDATE primary_keys  SET next_val = '8' WHERE name = 'unit_id'"
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

    public boolean construct() {
        if (execute(SQL_CREATE_TABLE_PRIMARY_KEYS)
                && execute(SQL_CREATE_TABLE_SETTINGS)
                && execute(SQL_CREATE_TABLE_UNITS)
                && execute(SQL_CREATE_TABLE_PRODUCTS)
                && execute(SQL_CREATE_TABLE_TAGS)
                && execute(SQL_CREATE_TABLE_RECIPES)
                && execute(SQL_CREATE_TABLE_RECIPES_TAGS)
                && execute(SQL_CREATE_TABLE_INGREDIENTS)
                && execute(SQL_CREATE_TABLE_PROFILES)
                && execute(SQL_CREATE_TABLE_WEEKS)
                && execute(SQL_CREATE_TABLE_DAYS)
                && execute(SQL_CREATE_TABLE_MEALS)
                && execute(SQL_CREATE_TABLE_MEALS_CONTENTS)) {
            for (String query : SQL_INSERT_DATA_LINES) {
                if (!execute(query)) {
                    return false;
                }
            }
        } else {
            return false;
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
