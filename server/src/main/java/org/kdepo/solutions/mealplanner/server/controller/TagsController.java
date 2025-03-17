package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Tag;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.TagsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Optional;

@Controller
@RequestMapping("/tags")
public class TagsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagsController.class);

    private static final String PK = "tag_id";

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping
    public String showTagsListPage(Model model) {
        LOGGER.trace("[WEB] GET /tags");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Prepare entities
        List<Tag> tags = tagsRepository.getAllTags();
        model.addAttribute("tags", tags);

        return "tags_list";
    }

    @GetMapping("/{id}")
    public String showTagDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /tags/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot show tag details page: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canReadTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot show tag details page: user '{}' has no access to tag {}", userName, id);
            return "redirect:/tags";
        }

        // Prepare entities
        model.addAttribute("tag", tag);

        List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        model.addAttribute("recipes", recipes);

        return "tag_details";
    }

    @GetMapping("/create")
    public String showTagCreationForm(Model model) {
        LOGGER.trace("[WEB] GET /tags/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show tag creation form: anonymous users cannot create tags");
            return "redirect:/tags";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateTag(userName)) {
            LOGGER.warn("[WEB] Cannot show tag creation form: user '{}' cannot create tags", userName);
            return "redirect:/tags";
        }

        // Prepare entity with default values
        Tag tag = new Tag();
        tag.setTagId(-1);
        model.addAttribute("tag", tag);

        return "tag_create";
    }

    @PostMapping("/create")
    public String acceptTagCreationForm(@Valid Tag tag, BindingResult result) {
        LOGGER.trace("[WEB] POST /tags/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept tag creation form: anonymous users cannot create tags");
            return "redirect:/tags";
        }

        // Operation availability checks
        if (!controlService.canCreateTag(userName)) {
            LOGGER.warn("[WEB] Cannot accept tag creation form: user '{}' cannot create tags", userName);
            return "redirect:/tags";
        }

        // Validate that provided data is correct
        String tagName = tag.getName();
        if (tagName == null || tagName.isEmpty()) {
            FieldError fieldError = new FieldError("tag", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "tag_create";
        }

        if (tagName.length() > 50) {
            FieldError fieldError = new FieldError("tag", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "tag_create";
        }

        List<Tag> allTags = tagsRepository.getAllTags();
        for (Tag tagToValidate : allTags) {
            if (tagToValidate.getName().equalsIgnoreCase(tagName)) {
                FieldError fieldError = new FieldError("tag", "name", "Объект с таким именем уже существует!");
                result.addError(fieldError);
                return "tag_create";
            }
        }

        if (tag.getDescription() != null && tag.getDescription().length() > 200) {
            FieldError fieldError = new FieldError("tag", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "tag_create";
        }

        // Generate primary key for new entity
        Integer tagId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        tag.setTagId(tagId);

        // Create entity
        Tag createdTag = tagsRepository.addTag(tag.getTagId(), tag.getName(), tag.getDescription());

        // Register operation in system events log
        logService.registerTagCreated(userName, createdTag);

        return "redirect:/tags/" + tag.getTagId();
    }

    @GetMapping("/{id}/update")
    public String showTagModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /tags/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show tag modification form: anonymous users cannot modify tags");
            return "redirect:/tags/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot show tag modification form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canModifyTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot show tag modification form: user '{}' has no access to tag {} modification", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        model.addAttribute("tag", tag);

        return "tag_update";
    }

    @PostMapping("/{id}/update")
    public String acceptTagModificationForm(@Valid Tag tag, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /tags/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept tag modification form: anonymous users cannot modify tags");
            return "redirect:/tags/" + id;
        }

        Tag tagFromDb = tagsRepository.getTag(id);
        if (tagFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept tag modification form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canModifyTag(userName, tagFromDb.getTagId())) {
            LOGGER.warn("[WEB] Cannot accept tag modification form: user '{}' has no access to tag {} modification", userName, id);
            return "redirect:/tags/" + tagFromDb.getTagId();
        }

        // Validate that provided data is correct
        String tagName = tag.getName();
        if (tagName == null || tagName.isEmpty()) {
            FieldError fieldError = new FieldError("tag", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "tag_update";
        }

        if (tagName.length() > 50) {
            FieldError fieldError = new FieldError("tag", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "tag_update";
        }

        List<Tag> allTags = tagsRepository.getAllTags();
        for (Tag tagToValidate : allTags) {
            if (tagToValidate.getName().equalsIgnoreCase(tagName)
                    && !tagToValidate.getTagId().equals(id)) {
                FieldError fieldError = new FieldError("tag", "name", "Объект с таким именем уже существует!");
                result.addError(fieldError);
                return "tag_update";
            }
        }

        if (tag.getDescription() != null && tag.getDescription().length() > 200) {
            FieldError fieldError = new FieldError("tag", "description", "Примечание не может быть длиннее 200 символов!");
            result.addError(fieldError);
            return "tag_update";
        }

        // Update entity
        tagsRepository.updateTag(tag.getTagId(), tag.getName(), tag.getDescription());

        // Register operation in system events log
        logService.registerTagUpdated(userName, tagFromDb, tag);

        return "redirect:/tags/" + tag.getTagId();
    }

    @GetMapping("/{id}/delete")
    public String showTagDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /tags/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show tag deletion form: anonymous users cannot delete tags");
            return "redirect:/tags/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot show tag deletion form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canDeleteTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot show tag deletion form: user '{}' has no access to tag {} deletion", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        model.addAttribute("tag", tag);

        return "tag_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptTagDeletionForm(@PathVariable Integer id) {
        LOGGER.trace("[WEB] POST /tags/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept tag deletion form: anonymous users cannot delete tags");
            return "redirect:/tags/" + id;
        }

        Tag tagFromDb = tagsRepository.getTag(id);
        if (tagFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept tag deletion form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canDeleteTag(userName, tagFromDb.getTagId())) {
            LOGGER.warn("[WEB] Cannot accept tag deletion form: user '{}' has no access to tag {} deletion", userName, id);
            return "redirect:/tags/" + tagFromDb.getTagId();
        }

        // Delete tag bindings
        List<Recipe> recipesByTag = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tagFromDb.getTagId()));
        for (Recipe recipe : recipesByTag) {
            tagsRepository.deleteTagFromRecipe(tagFromDb.getTagId(), recipe.getRecipeId());

            // TODO register unbind?
            //logService.registerTagDeleted(userName, tagFromDb.getTagId());
        }

        // Delete entity
        tagsRepository.deleteTag(tagFromDb.getTagId());

        // Register operation in system events log
        logService.registerTagDeleted(userName, tagFromDb.getTagId());

        return "redirect:/tags";
    }

    @GetMapping("/{id}/set")
    public String showTagSetForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /tags/{}/set", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show tag set form: anonymous users cannot set tag on recipes");
            return "redirect:/tags/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot show tag set form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canSetTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot show tag set form: user '{}' has no access to tag {} set", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        model.addAttribute("tag", tag);

        List<Recipe> recipesAll = recipesRepository.getAllRecipes();
        List<Recipe> recipesWithTag = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        List<Integer> recipesIdWithTag = recipesWithTag.stream()
                .map(Recipe::getRecipeId)
                .toList();
        List<Recipe> recipes = recipesAll.stream()
                .filter(recipe -> !recipesIdWithTag.contains(recipe.getRecipeId()))
                .toList();
        model.addAttribute("recipes", recipes);

        return "tag_set";
    }

    @PostMapping("/{id}/set")
    public String acceptTagSetForm(@PathVariable Integer id,
                                   @RequestParam(value = "selectedRecipes", required = false) Optional<List<Integer>> selectedRecipes) {
        LOGGER.trace("[WEB] POST /tags/{}/set", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept tag set form: anonymous users cannot set tag on recipes");
            return "redirect:/tags/" + id;
        }

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot accept tag set form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canSetTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot accept tag set form: user '{}' has no access to tag {} set", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        List<Recipe> recipesWithTag = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        List<Integer> recipesIdWithTag = recipesWithTag.stream()
                .map(Recipe::getRecipeId)
                .toList();

        List<Integer> recipes = selectedRecipes.orElse(Collections.emptyList());
        for (Integer recipeId : recipes) {
            Recipe recipe = recipesRepository.getRecipe(recipeId);
            if (recipe != null && !recipesIdWithTag.contains(recipe.getRecipeId())) {
                tagsRepository.addTagToRecipe(id, recipeId);

                // Register operation in system events log
                logService.registerTagSet(userName, tag.getTagId(), recipe.getRecipeId());
            }
        }

        return "redirect:/tags/" + id;
    }

    @GetMapping("/{id}/unset")
    public String showTagUnsetForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /tags/{}/unset", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show tag unset form: anonymous users cannot unset tag from recipes");
            return "redirect:/tags/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot show tag unset form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canUnsetTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot show tag unset form: user '{}' has no access to tag {} unset", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        model.addAttribute("tag", tag);

        List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
        model.addAttribute("recipes", recipes);

        return "tag_unset";
    }

    @PostMapping("/{id}/unset")
    public String acceptTagUnsetForm(@PathVariable Integer id,
                                     @RequestParam(value = "selectedRecipes", required = false) Optional<List<Integer>> selectedRecipes) {
        LOGGER.trace("[WEB] POST /tags/{}/unset", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept tag unset form: anonymous users cannot unset tags from recipes");
            return "redirect:/tags/" + id;
        }

        Tag tag = tagsRepository.getTag(id);
        if (tag == null) {
            LOGGER.warn("[WEB] Cannot accept tag unset form: tag {} was not found", id);
            return "redirect:/tags";
        }

        if (!controlService.canUnsetTag(userName, tag.getTagId())) {
            LOGGER.warn("[WEB] Cannot accept tag unset form: user '{}' has no access to tag {} unset", userName, id);
            return "redirect:/tags/" + tag.getTagId();
        }

        List<Integer> recipes = selectedRecipes.orElse(Collections.emptyList());
        for (Integer recipeId : recipes) {
            Recipe recipe = recipesRepository.getRecipe(recipeId);
            if (recipe != null) {
                tagsRepository.deleteTagFromRecipe(id, recipeId);

                // Register operation in system events log
                logService.registerTagUnset(userName, tag.getTagId(), recipe.getRecipeId());
            }
        }

        return "redirect:/tags/" + id;
    }
}
