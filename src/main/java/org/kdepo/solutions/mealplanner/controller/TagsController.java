package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Tag;
import org.kdepo.solutions.mealplanner.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/tags")
public class TagsController {

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

    @GetMapping("/{tid}")
    public String showDeviceDetailsPage(@PathVariable Integer tid, Model model) {
        System.out.println("[WEB]" + " GET " + "/tags/" + tid);

        Tag tag = tagsRepository.getTag(tid);
        if (tag != null) {
            model.addAttribute("tag", tag);

            List<Recipe> recipes = recipesRepository.getAllRecipes(Collections.emptyList(), Collections.singletonList(tag.getTagId()));
            model.addAttribute("recipes", recipes);

            return "tag_details";
        } else {
            return "redirect:/tags_list";
        }
    }

}
