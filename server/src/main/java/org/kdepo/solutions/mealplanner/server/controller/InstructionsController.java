package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.InstructionStep;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.repository.InstructionsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
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

import java.util.List;

@Controller
@RequestMapping("/instructions")
public class InstructionsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructionsController.class);

    private static final String PK = "instruction_step_id";

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private InstructionsRepository instructionsRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/{id}")
    public String showInstructionStepDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /instructions/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        InstructionStep instructionStep = instructionsRepository.getInstructionStep(id);
        if (instructionStep == null) {
            LOGGER.warn("[WEB] Cannot show instructionStep details page: instructionStep {} was not found", id);
            return "redirect:/recipes_list";
        }

        if (!controlService.canReadInstructionStep(userName, instructionStep.getInstructionStepId())) {
            LOGGER.warn("[WEB] Cannot show instructionStep details page: user '{}' has no access to instructionStep {}", userName, id);
            return "redirect:/recipes_list";
        }

        model.addAttribute("instructionStep", instructionStep);

        return "instruction_step_1_details";
    }

    @GetMapping("/create")
    public String showInstructionStepCreationForm(Model model,
                                                  @RequestParam("instruction_type_id") Integer instructionTypeId,
                                                  @RequestParam("recipe_id") Integer recipeId) {
        LOGGER.trace("[WEB] GET /instructions/create?instruction_type_id={}&recipe_id={}", instructionTypeId, recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show instruction step creation form: anonymous users cannot create instructions steps");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateInstructionStep(userName, recipeId)) {
            LOGGER.warn("[WEB] Cannot show instruction step creation form: user '{}' cannot create instructions steps", userName);
            return "redirect:/recipes";
        }

        if (instructionTypeId != 1 && instructionTypeId != 2) {
            LOGGER.warn("[WEB] Cannot show instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            LOGGER.warn("[WEB] Cannot show instruction step creation form: recipe {} was not found", recipeId);
            return "redirect:/recipes";
        }

        String templateName = null;

        InstructionStep instructionStep = new InstructionStep();
        instructionStep.setInstructionStepId(-1);
        instructionStep.setRecipeId(recipeId);

        if (Constants.InstructionType.PLAIN_TEXT.equals(instructionTypeId)) {
            instructionStep.setName(null);
            instructionStep.setDescription(null);
            instructionStep.setImage(null);
            instructionStep.setOrderNumber(1);

            templateName = "instruction_step_1_create";

        } else if (Constants.InstructionType.STEP_BY_STEP.equals(instructionTypeId)) {
            List<InstructionStep> steps = instructionsRepository.getAllInstructionStepsFromRecipe(recipeId);
            int orderNumber = steps.size() + 1;

            instructionStep.setName("Шаг " + orderNumber);
            instructionStep.setDescription(null);
            instructionStep.setImage(null);
            instructionStep.setOrderNumber(orderNumber);

            templateName = "instruction_step_2_create";
        }

        model.addAttribute("instructionStep", instructionStep);

        return templateName;
    }

    @PostMapping("/create")
    public String acceptInstructionStepCreationForm(@Valid InstructionStep instructionStep,
                                                    @RequestParam("instruction_type_id") Integer instructionTypeId,
                                                    @RequestParam("recipe_id") Integer recipeId,
                                                    BindingResult result) {
        LOGGER.trace("[WEB] POST /instructions/create?instruction_type_id={}&recipe_id={}", instructionTypeId, recipeId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept instruction step creation form: anonymous users cannot create instructions steps");
            return "redirect:/recipes";
        }

        // Operation availability checks
        if (!controlService.canCreateInstructionStep(userName, recipeId)) {
            LOGGER.warn("[WEB] Cannot accept instruction step creation form: user '{}' cannot create instructions steps for recipe {}", userName, recipeId);
            return "redirect:/recipes";
        }

        Recipe recipe = recipesRepository.getRecipe(recipeId);
        if (recipe == null) {
            LOGGER.warn("[WEB] Cannot accept instruction step creation form: recipe {} was not found", recipeId);
            return "redirect:/recipes";
        }

        // Validate that provided data is correct
        if (Constants.InstructionType.PLAIN_TEXT.equals(recipe.getInstructionTypeId())) {
            LOGGER.warn("[WEB] Cannot accept instruction step creation form: recipe {} has instructionTypeId {} and cannot have more steps", recipeId, instructionTypeId);
            return "redirect:/recipes";
        }

        if (instructionTypeId != 1 && instructionTypeId != 2) {
            LOGGER.warn("[WEB] Cannot accept instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
            return "redirect:/recipes";
        }

        String name = instructionStep.getName();
        if (name == null || name.isEmpty()) {
            FieldError fieldError = new FieldError("instructionStep", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            if (instructionTypeId == 1) {
                return "instruction_step_1_create";
            } else if (instructionTypeId == 2) {
                return "instruction_step_2_create";
            } else {
                LOGGER.error("[WEB] Cannot accept instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
                return "redirect:/recipes";
            }
        }

        if (name.length() > 50) {
            FieldError fieldError = new FieldError("instructionStep", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            if (instructionTypeId == 1) {
                return "instruction_step_1_create";
            } else if (instructionTypeId == 2) {
                return "instruction_step_2_create";
            } else {
                LOGGER.error("[WEB] Cannot accept instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
                return "redirect:/recipes";
            }
        }

        String description = instructionStep.getDescription();
        if (description == null || description.isEmpty()) {
            FieldError fieldError = new FieldError("instructionStep", "description", "Поле не может быть пустым!");
            result.addError(fieldError);
            if (instructionTypeId == 1) {
                return "instruction_step_1_create";
            } else if (instructionTypeId == 2) {
                return "instruction_step_2_create";
            } else {
                LOGGER.error("[WEB] Cannot accept instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
                return "redirect:/recipes";
            }
        }

        if (description.length() > 2000) {
            FieldError fieldError = new FieldError("instructionStep", "description", "Описание не может быть длиннее 2000 символов!");
            result.addError(fieldError);
            if (instructionTypeId == 1) {
                return "instruction_step_1_create";
            } else if (instructionTypeId == 2) {
                return "instruction_step_2_create";
            } else {
                LOGGER.error("[WEB] Cannot accept instruction step creation form: instructionTypeId {} is unknown", instructionTypeId);
                return "redirect:/recipes";
            }
        }

        // Generate primary key for new entity
        Integer instructionStepId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        instructionStep.setInstructionStepId(instructionStepId);

        int orderNumber = instructionsRepository.getAllInstructionStepsFromRecipe(recipeId).size() + 1;
        instructionStep.setOrderNumber(orderNumber);

        // Create entity
        InstructionStep instructionStepCreated = instructionsRepository.addInstructionStep(
                instructionStep.getInstructionStepId(),
                instructionStep.getRecipeId(),
                instructionStep.getName(),
                instructionStep.getDescription(),
                instructionStep.getImage(),
                instructionStep.getOrderNumber()
        );
        // Register operation in system events log
        logService.registerInstructionStepCreated(userName, instructionStepCreated);

        if (Constants.InstructionType.UNDEFINED.equals(recipe.getInstructionTypeId())) {
            recipe.setInstructionTypeId(instructionStepId);
            recipesRepository.updateRecipe(
                    recipe.getRecipeId(),
                    recipe.getInstructionTypeId(),
                    recipe.getName(),
                    recipe.getDescription(),
                    recipe.getSource(),
                    recipe.getPortions(),
                    recipe.getWeight(),
                    recipe.getCalories(),
                    recipe.getProteins(),
                    recipe.getFats(),
                    recipe.getCarbs()
            );

            Recipe recipeFromDb = recipesRepository.getRecipe(recipeId);

            logService.registerRecipeUpdated(userName, recipeFromDb, recipe);
        }

        return "redirect:/recipes/" + recipeId;
    }

}
