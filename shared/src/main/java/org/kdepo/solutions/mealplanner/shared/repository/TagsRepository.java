package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Tag;

import java.util.List;

public interface TagsRepository {

    Tag addTag(Integer tagId, String name, String description);

    void addTagToRecipe(Integer tagId, Integer recipeId);

    void deleteTag(Integer tagId);

    void deleteTagFromRecipe(Integer tagId, Integer recipeId);

    List<Tag> getAllTags();

    List<Tag> getAllTagsForRecipe(Integer recipeId);

    Tag getTag(Integer tagId);

    void updateTag(Integer tagId, String name, String description);


}
