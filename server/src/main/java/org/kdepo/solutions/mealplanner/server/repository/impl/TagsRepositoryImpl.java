package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.repository.TagsRepository;
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
public class TagsRepositoryImpl implements TagsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagsRepositoryImpl.class);

    private static final String SQL_ADD_TAG = "INSERT INTO tags (tag_id, name, description) VALUES (?, ?, ?)";
    private static final String SQL_ADD_TAG_TO_RECIPE = "INSERT INTO recipes_tags (recipe_id, tag_id) VALUES (?, ?)";
    private static final String SQL_DELETE_TAG = "DELETE FROM tags WHERE tag_id = ?";
    private static final String SQL_DELETE_TAG_FROM_RECIPE = "DELETE FROM recipes_tags WHERE recipe_id = ? AND tag_id = ?";
    private static final String SQL_GET_ALL_TAGS = "SELECT * FROM tags ORDER BY name ASC";
    private static final String SQL_GET_ALL_TAGS_FOR_RECIPE = "SELECT rt.tag_id, t.name, t.description FROM recipes_tags rt JOIN tags t ON t.tag_id = rt.tag_id WHERE rt.recipe_id = ?";
    private static final String SQL_GET_TAG = "SELECT * FROM tags WHERE tag_id = ?";
    private static final String SQL_UPDATE_TAG = "UPDATE tags SET name = ?, description = ? WHERE tag_id = ?";

    private static final List<Integer> NOT_FOR_DELETE = List.of(
            1 // Hidden
    );

    private final JdbcTemplate jdbcTemplate;

    public TagsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Tag addTag(Integer tagId, String name, String description) {
        LOGGER.trace("[DBR][addTag] Invoked with parameters: tagId={}, name={}, description={}",
                tagId, name, description
        );
        jdbcTemplate.update(
                SQL_ADD_TAG,
                ps -> {
                    ps.setInt(1, tagId);
                    ps.setString(2, name);
                    ps.setString(3, description);
                }
        );
        return getTag(tagId);
    }

    @Override
    public void addTagToRecipe(Integer tagId, Integer recipeId) {
        LOGGER.trace("[DBR][addTagToRecipe] Invoked with parameters: tagId={}, recipeId={}",
                tagId, recipeId
        );
        jdbcTemplate.update(
                SQL_ADD_TAG_TO_RECIPE,
                ps -> {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, tagId);
                }
        );
    }

    @Override
    public void deleteTag(Integer tagId) {
        LOGGER.trace("[DBR][deleteTag] Invoked with parameters: tagId={}", tagId);

        if (NOT_FOR_DELETE.contains(tagId)) {
            return;
        }

        jdbcTemplate.update(
                SQL_DELETE_TAG,
                ps -> ps.setInt(1, tagId)
        );
    }

    @Override
    public void deleteTagFromRecipe(Integer tagId, Integer recipeId) {
        LOGGER.trace("[DBR][deleteTagFromRecipe] Invoked with parameters: tagId={}, recipeId={}",
                tagId, recipeId
        );
        jdbcTemplate.update(
                SQL_DELETE_TAG_FROM_RECIPE,
                ps -> {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, tagId);
                }
        );
    }

    @Override
    public List<Tag> getAllTags() {
        LOGGER.trace("[DBR][getAllTags] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_TAGS,
                rs -> {
                    List<Tag> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public List<Tag> getAllTagsForRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][getAllTagsForRecipe] Invoked with parameters: recipeId={}", recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_TAGS_FOR_RECIPE,
                ps -> ps.setInt(1, recipeId),
                rs -> {
                    List<Tag> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Tag getTag(Integer tagId) {
        LOGGER.trace("[DBR][getTag] Invoked with parameters: tagId={}", tagId);
        return jdbcTemplate.query(
                SQL_GET_TAG,
                ps -> ps.setInt(1, tagId),
                rs -> {
                    Tag tag = null;
                    if (rs.next()) {
                        tag = convert(rs);
                    }
                    return tag;
                }
        );
    }

    @Override
    public void updateTag(Integer tagId, String name, String description) {
        LOGGER.trace("[DBR][updateTag] Invoked with parameters: tagId={}, name={}, description={}",
                tagId, name, description
        );
        jdbcTemplate.update(
                SQL_UPDATE_TAG,
                ps -> {
                    ps.setString(1, name);
                    ps.setString(2, description);
                    ps.setInt(3, tagId);
                }
        );
    }

    private Tag convert(ResultSet rs) throws SQLException {
        Integer tagId = rs.getInt("tag_id");
        String name = rs.getString("name");
        String description = rs.getString("description");

        Tag tag = new Tag();
        tag.setTagId(tagId);
        tag.setName(name);
        tag.setDescription(description);

        return tag;
    }
}
