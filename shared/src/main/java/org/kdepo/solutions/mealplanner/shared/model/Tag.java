package org.kdepo.solutions.mealplanner.shared.model;

import java.util.Objects;

public class Tag {

    private Integer tagId;
    private String name;
    private String description;

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagId, tag.tagId)
                && Objects.equals(name, tag.name)
                && Objects.equals(description, tag.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, name, description);
    }
}
