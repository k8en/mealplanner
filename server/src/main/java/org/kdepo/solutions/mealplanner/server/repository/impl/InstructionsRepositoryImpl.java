package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.InstructionStep;
import org.kdepo.solutions.mealplanner.shared.repository.InstructionsRepository;
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
public class InstructionsRepositoryImpl implements InstructionsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructionsRepositoryImpl.class);

    private static final String SQL_ADD_INSTRUCTION_STEP = "INSERT INTO instructions_steps (instruction_step_id, recipe_id, name, description, image, order_number) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_INSTRUCTION_STEP = "DELETE FROM instructions_steps WHERE instruction_step_id = ?";
    private static final String SQL_GET_ALL_INSTRUCTION_STEPS_FROM_RECIPE = "SELECT * FROM instructions_steps WHERE recipe_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_INSTRUCTION_STEP = "SELECT * FROM instructions_steps WHERE instruction_step_id = ?";
    private static final String SQL_UPDATE_INSTRUCTION_STEP = "UPDATE instructions_steps SET recipe_id = ?, name = ?, description = ?, image = ?, order_number = ? WHERE instruction_step_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public InstructionsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public InstructionStep addInstructionStep(Integer instructionStepId, Integer recipeId, String name, String description, String image, Integer orderNumber) {
        LOGGER.trace("[DBR][addInstructionStep] Invoked with parameters: instructionStepId={}, recipeId={}, name={}, description={}, image={}, orderNumber={}",
                instructionStepId, recipeId, name, description, image, orderNumber
        );
        jdbcTemplate.update(
                SQL_ADD_INSTRUCTION_STEP,
                ps -> {
                    ps.setInt(1, instructionStepId);
                    ps.setInt(2, recipeId);
                    ps.setString(3, name);
                    ps.setString(4, description);
                    ps.setString(5, image);
                    ps.setInt(6, orderNumber);
                }
        );
        return getInstructionStep(instructionStepId);
    }

    @Override
    public void deleteInstructionStep(Integer instructionStepId) {
        LOGGER.trace("[DBR][deleteInstructionStep] Invoked with parameters: instructionStepId={}", instructionStepId);
        jdbcTemplate.update(
                SQL_DELETE_INSTRUCTION_STEP,
                ps -> ps.setInt(1, instructionStepId)
        );
    }

    @Override
    public List<InstructionStep> getAllInstructionStepsFromRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][getAllInstructionStepsFromRecipe] Invoked with parameters: recipeId={}", recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_INSTRUCTION_STEPS_FROM_RECIPE,
                ps -> ps.setInt(1, recipeId),
                rs -> {
                    List<InstructionStep> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public InstructionStep getInstructionStep(Integer instructionStepId) {
        LOGGER.trace("[DBR][getInstructionStep] Invoked with parameters: instructionStepId={}", instructionStepId);
        return jdbcTemplate.query(
                SQL_GET_INSTRUCTION_STEP,
                ps -> ps.setInt(1, instructionStepId),
                rs -> {
                    InstructionStep instructionStep = null;
                    if (rs.next()) {
                        instructionStep = convert(rs);
                    }
                    return instructionStep;
                }
        );
    }

    @Override
    public void updateInstructionStep(Integer instructionStepId, Integer recipeId, String name, String description, String image, Integer orderNumber) {
        LOGGER.trace("[DBR][updateInstructionStep] Invoked with parameters: instructionStepId={}, recipeId={}, name={}, description={}, image={}, orderNumber={}",
                instructionStepId, recipeId, name, description, image, orderNumber
        );
        jdbcTemplate.update(
                SQL_UPDATE_INSTRUCTION_STEP,
                ps -> {
                    ps.setInt(1, recipeId);
                    ps.setString(2, name);
                    ps.setString(3, description);
                    ps.setString(4, image);
                    ps.setInt(5, orderNumber);
                    ps.setInt(6, instructionStepId);
                }
        );
    }

    private InstructionStep convert(ResultSet rs) throws SQLException {
        Integer instructionStepId = rs.getInt("instruction_step_id");
        Integer recipeId = rs.getInt("recipe_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String image = rs.getString("image");
        Integer orderNumber = rs.getInt("order_number");

        InstructionStep instructionStep = new InstructionStep();
        instructionStep.setInstructionStepId(instructionStepId);
        instructionStep.setRecipeId(recipeId);
        instructionStep.setName(name);
        instructionStep.setDescription(description);
        instructionStep.setImage(image);
        instructionStep.setOrderNumber(orderNumber);

        return instructionStep;
    }
}
