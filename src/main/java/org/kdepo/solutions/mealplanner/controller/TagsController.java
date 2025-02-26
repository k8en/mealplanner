package org.kdepo.solutions.mealplanner.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.kdepo.solutions.mealplanner.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/tags")
public class TagsController {

    private static final String PK = "tag_id";

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @GetMapping
    public String showTagsListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/tags");

        List<Tag> tags = tagsRepository.getAllTags();
        model.addAttribute("tags", tags);

        return "tags_list";
    }

    @GetMapping("/{id}")
    public String showTagDetailsPage(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id);

        Tag tag = tagsRepository.getTag(id);
        if (tag != null) {
            model.addAttribute("tag", tag);

            List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
            model.addAttribute("recipes", recipes);

            return "tag_details";
        } else {
            return "redirect:/tags_list";
        }
    }

    @GetMapping("/create")
    public String showTagCreationForm(Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/create");

        Tag tag = new Tag();
        tag.setTagId(-1);
        model.addAttribute("tag", tag);

        return "tag_create";
    }

    @PostMapping("/create")
    public String acceptTagCreationForm(@Valid Tag tag, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/tags/create");

        // Validate that this operation is allowed by the current user
        // TODO

        // Validate that provided data is correct
        String tagName = tag.getName();
        if (tagName == null || tagName.isEmpty()) {
            FieldError nameFieldError = new FieldError("tag", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "tag_create";
        }

        if (tagName.length() > 20) {
            FieldError nameFieldError = new FieldError("tag", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "tag_create";
        }

        List<Tag> allTags = tagsRepository.getAllTags();
        for (Tag tagToValidate : allTags) {
            if (tagToValidate.getName().equalsIgnoreCase(tagName)) {
                FieldError nameFieldError = new FieldError("tag", "name", "Объект с таким именем уже существует!");
                result.addError(nameFieldError);
                return "tag_create";
            }
        }

        if (tag.getDescription() != null && tag.getDescription().length() > 200) {
            FieldError nameFieldError = new FieldError("tag", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(nameFieldError);
            return "tag_create";
        }

        // Generate primary key for new entity
        Integer tagId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        tag.setTagId(tagId);

        // Create entity
        tagsRepository.addTag(tag.getTagId(), tag.getName(), tag.getDescription());

        // Register operation in system events log
        // TODO

        return "redirect:/tags/" + tag.getTagId();
    }

    @GetMapping("/{id}/update")
    public String showTagModificationForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/update");

        // Validate that this operation is allowed by the current user
        // TODO

        Tag tag = tagsRepository.getTag(id);
        if (tag != null) {
            model.addAttribute("tag", tag);
            return "tag_update";
        } else {
            return "redirect:/tags";
        }
    }

    @PostMapping("/{id}/update")
    public String acceptTagModificationForm(@Valid Tag tag, @PathVariable Integer id, BindingResult result) {
        System.out.println("[WEB]" + " POST " + "/tags/" + id + "/update");

        // Validate that this operation is allowed by the current user
        // TODO

        // Validate that provided data is correct
        String tagName = tag.getName();
        if (tagName == null || tagName.isEmpty()) {
            FieldError nameFieldError = new FieldError("tag", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "tag_update";
        }

        if (tagName.length() > 20) {
            FieldError nameFieldError = new FieldError("tag", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "tag_update";
        }

        List<Tag> allTags = tagsRepository.getAllTags();
        for (Tag tagToValidate : allTags) {
            if (tagToValidate.getName().equalsIgnoreCase(tagName)
                    && !tagToValidate.getTagId().equals(id)) {
                FieldError nameFieldError = new FieldError("tag", "name", "Объект с таким именем уже существует!");
                result.addError(nameFieldError);
                return "tag_update";
            }
        }

        if (tag.getDescription() != null && tag.getDescription().length() > 200) {
            FieldError nameFieldError = new FieldError("tag", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(nameFieldError);
            return "tag_update";
        }

        // Validate that object is exist
        boolean isExist = false;
        for (Tag tagToValidate : allTags) {
            if (tagToValidate.getTagId().equals(tag.getTagId())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            return "redirect:/tags";
        }

        // Update entity
        tagsRepository.updateTag(tag.getTagId(), tag.getName(), tag.getDescription());

        // Register operation in system events log
        // TODO

        return "redirect:/tags/" + tag.getTagId();
    }

    @GetMapping("/{id}/delete")
    public String showTagDeletionForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/delete");

        // Validate that this operation is allowed by the current user
        // TODO

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            return "redirect:/tags";
        }
        model.addAttribute("tag", tag);

        return "tag_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptTagDeletionForm(@PathVariable Integer id) {
        System.out.println("[WEB]" + " POST " + "/tags/" + id + "/delete");

        // Validate that this operation is allowed by the current user
        // TODO

        if (id == null) {
            System.out.println("[WEB] Error! Tag id not provided!");
            return "redirect:/tags";
        }

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            return "redirect:/tags";
        }

        // Delete tag bindings
        List<Recipe> recipesByTag = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        for (Recipe recipe : recipesByTag) {
            tagsRepository.deleteTagFromRecipe(tag.getTagId(), recipe.getRecipeId());
        }

        // Delete entity
        tagsRepository.deleteTag(tag.getTagId());

        // Register operation in system events log
        // TODO

        return "redirect:/tags";
    }

    @GetMapping("/{id}/set")
    public String showTagSetForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/set");

        // Validate that this operation is allowed by the current user
        // TODO

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            return "redirect:/tags";
        }
        model.addAttribute("tag", tag);

        List<Recipe> recipes = recipesRepository.getAllRecipes();
        model.addAttribute("recipes", recipes);

        return "tag_set";
    }

    @PostMapping("/{id}/set")
    public String acceptTagSetForm(@PathVariable Integer id, @RequestParam("selectedRecipes") List<Integer> selectedRecipes) {
        System.out.println("[WEB]" + " POST " + "/tags/" + id + "/set");

        // Validate that this operation is allowed by the current user
        // TODO

        for (Integer recipeId : selectedRecipes) {
            tagsRepository.addTagToRecipe(id, recipeId);
        }

        return "redirect:/tags/" + id;
    }

    @GetMapping("/{id}/unset")
    public String showTagUnsetForm(@PathVariable Integer id, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + id + "/unset");

        // Validate that this operation is allowed by the current user
        // TODO

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            return "redirect:/tags";
        }
        model.addAttribute("tag", tag);

        List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        model.addAttribute("recipes", recipes);

        return "tag_unset";
    }

    @PostMapping("/{id}/unset")
    public String acceptTagUnsetForm(@PathVariable Integer id, @RequestParam("selectedRecipes") List<Integer> selectedRecipes) {
        System.out.println("[WEB]" + " POST " + "/tags/" + id + "/unset");

        // Validate that this operation is allowed by the current user
        // TODO

        for (Integer recipeId : selectedRecipes) {
            tagsRepository.deleteTagFromRecipe(id, recipeId);
        }

        return "redirect:/tags/" + id;
    }

}
