package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.Profile;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.ProfilesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.WeeksRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/weeks")
public class WeeksController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeksController.class);

    private static final String PK = "week_id";

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/create")
    public String showWeekCreationForm(Model model, @RequestParam(value = "profile_id") Integer profileId) {
        LOGGER.trace("[WEB] GET /weeks/create?profile_id={}", profileId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show week creation form: anonymous users cannot create weeks");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateWeek(userName)) {
            LOGGER.warn("[WEB] Cannot show week creation form: user '{}' cannot create weeks", userName);
            return "redirect:/profiles";
        }

        if (profileId == null) {
            LOGGER.warn("[WEB] Cannot show week creation form: profile is not provided for week creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show week creation form: profile {} was not found", profileId);
            return "redirect:/profiles";
        }
        if (Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot show week creation form: profile {} type mismatch", profileId);
            return "redirect:/profiles";
        }
        model.addAttribute("profile", profile);

        Week week = new Week();
        week.setWeekId(-1);
        week.setProfileId(profileId);

        List<Week> weeksList = weeksRepository.getAllWeeksFromProfile(profileId);
        int orderNumber = weeksList.size() + 1;
        week.setName("Неделя " + orderNumber);
        week.setOrderNumber(orderNumber);
        model.addAttribute("week", week);

        return "week_create";
    }

    @PostMapping("/create")
    public String acceptWeekCreationForm(@Valid Week week, BindingResult result) {
        LOGGER.trace("[WEB] POST /weeks/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept week creation form: anonymous users cannot create weeks");
            return "redirect:/profiles";
        }

        // Operation availability checks
        if (!controlService.canCreateWeek(userName)) {
            LOGGER.warn("[WEB] Cannot accept week creation form: user '{}' cannot create weeks", userName);
            return "redirect:/profiles";
        }

        // Validate that provided data is correct
        String weekName = week.getName();
        if (weekName == null || weekName.isEmpty()) {
            FieldError fieldError = new FieldError("week", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "week_create";
        }

        if (weekName.length() > 20) {
            FieldError fieldError = new FieldError("week", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "week_create";
        }

        Integer profileId = week.getProfileId();

        if (profileId == null) {
            LOGGER.warn("[WEB] Profile is not provided for week creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot accept week creation form: profile {} was not found", profileId);
            return "redirect:/profiles";
        }
        if (Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot accept week creation form: profile {} is not suitable for grouping by weeks", profileId);
            return "redirect:/profiles";
        }

        // Generate primary key for new entity
        Integer weekId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        week.setWeekId(weekId);

        // Create entity
        Week createdWeek = weeksRepository.addWeek(
                week.getWeekId(),
                week.getProfileId(),
                week.getName(),
                week.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerWeekCreated(userName, createdWeek);

        // Check and update profile if necessary
        if (Constants.ProfileType.UNDEFINED.equals(profile.getProfileTypeId())) {
            Profile oldData = profilesRepository.getProfile(profileId);

            profile.setProfileTypeId(Constants.ProfileType.DAYS_GROUPED_BY_WEEKS);
            profilesRepository.updateProfile(
                    profile.getProfileId(),
                    profile.getProfileTypeId(),
                    profile.getName(),
                    profile.getActive()
            );

            // Register operation in system events log
            logService.registerProfileUpdated(userName, oldData, profile);
        }

        return "redirect:/profiles/" + profile.getProfileId();
    }

}
