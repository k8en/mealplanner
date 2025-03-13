package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TagsRepositoryImpl implements TagsRepository {

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
        System.out.println("[TagDao][addTag] Invoked with parameters:"
                + " tagId=" + tagId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
        );
        jdbcTemplate.update(SQL_ADD_TAG, tagId, name, description);

        return getTag(tagId);
    }

    @Override
    public void addTagToRecipe(Integer tagId, Integer recipeId) {
        System.out.println("[TagDao][addTagToRecipe] Invoked with parameters:"
                + " tagId=" + tagId
                + ", recipeId=" + recipeId
        );
        jdbcTemplate.update(SQL_ADD_TAG_TO_RECIPE, recipeId, tagId);
    }

    @Override
    public void deleteTag(Integer tagId) {
        System.out.println("[TagDao][deleteTag] Invoked with parameters: tagId=" + tagId);

        if (NOT_FOR_DELETE.contains(tagId)) {
            return;
        }

        jdbcTemplate.update(SQL_DELETE_TAG, tagId);
    }

    @Override
    public void deleteTagFromRecipe(Integer tagId, Integer recipeId) {
        System.out.println("[TagDao][deleteTagFromRecipe] Invoked with parameters:"
                + " tagId=" + tagId
                + ", recipeId=" + recipeId
        );
        jdbcTemplate.update(SQL_DELETE_TAG_FROM_RECIPE, recipeId, tagId);
    }

    @Override
    public List<Tag> getAllTags() {
        System.out.println("[TagDao][getAllTags] Invoked without parameters");
        return jdbcTemplate.query(
                SQL_GET_ALL_TAGS,
                (resultSet, rowNum) -> {
                    Integer tagId = resultSet.getInt("tag_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");

                    Tag tag = new Tag();
                    tag.setTagId(tagId);
                    tag.setName(name);
                    tag.setDescription(description);

                    return tag;
                }
        );
    }

    @Override
    public List<Tag> getAllTagsForRecipe(Integer recipeId) {
        System.out.println("[TagDao][getAllTagsForRecipe] Invoked with parameters: recipeId=" + recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_TAGS_FOR_RECIPE,
                (resultSet, rowNum) -> {
                    Integer tagId = resultSet.getInt("tag_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");

                    Tag tag = new Tag();
                    tag.setTagId(tagId);
                    tag.setName(name);
                    tag.setDescription(description);

                    return tag;
                },
                recipeId
        );
    }

    @Override
    public Tag getTag(Integer tagId) {
        System.out.println("[TagDao][getTag] Invoked with parameters: tagId=" + tagId);
        return jdbcTemplate.query(
                SQL_GET_TAG,
                resultSet -> {
                    //Integer tagId = resultSet.getInt("tag_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");

                    Tag tag = new Tag();
                    tag.setTagId(tagId);
                    tag.setName(name);
                    tag.setDescription(description);

                    return tag;
                },
                tagId
        );
    }

    @Override
    public void updateTag(Integer tagId, String name, String description) {
        System.out.println("[TagDao][updateTag] Invoked with parameters:"
                + " tagId=" + tagId
                + ", name='" + name + "'"
                + ", description='" + description + "'"
        );
        jdbcTemplate.update(SQL_UPDATE_TAG, name, description, tagId);
    }
}
