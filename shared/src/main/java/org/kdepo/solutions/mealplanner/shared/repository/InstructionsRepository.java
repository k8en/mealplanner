package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.InstructionStep;

import java.util.List;

public interface InstructionsRepository {

    InstructionStep addInstructionStep(Integer instructionStepId, Integer recipeId, String name, String description, String image, Integer orderNumber);

    void deleteInstructionStep(Integer instructionStepId);

    List<InstructionStep> getAllInstructionStepsFromRecipe(Integer recipeId);

    InstructionStep getInstructionStep(Integer instructionStepId);

    void updateInstructionStep(Integer instructionStepId, Integer recipeId, String name, String description, String image, Integer orderNumber);

}
