package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Profile;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
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
@RequestMapping("/days")
public class DaysController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaysController.class);

    private static final String PK = "day_id";

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping("/create")
    public String showDayCreationForm(Model model,
                                      @RequestParam(value = "profile_id", required = false) Integer profileId,
                                      @RequestParam(value = "week_id", required = false) Integer weekId) {
        LOGGER.trace("[WEB] GET /days/create?profile_id={}&week_id={}", profileId, weekId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day creation form: anonymous users cannot create days");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot show day creation form: user '{}' cannot create days", userName);
            return "redirect:/profiles";
        }

        if (profileId == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile is not provided for day creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} was not found", profileId);
            return "redirect:/profiles";
        }

        Week week = null;
        if (weekId != null) {
            week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot show day creation form: week {} was not found", weekId);
                return "redirect:/profiles";
            }
        }

        if (week != null && Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} type mismatch - week is provided, but this this is DAYS_WITHOUT_GROUPING", profileId);
            return "redirect:/profiles";
        }

        if (week == null && Constants.ProfileType.DAYS_GROUPED_BY_WEEKS.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} type mismatch - week is not provided, but this is DAYS_GROUPED_BY_WEEKS", profileId);
            return "redirect:/profiles";
        }

        model.addAttribute("profile", profile);
        model.addAttribute("week", week);

        String name;
        Integer orderNumber;
        if (week == null) {
            List<Day> daysList = daysRepository.getAllDaysFromProfile(profileId);
            orderNumber = daysList.size() + 1;
            name = "День " + orderNumber;
        } else {
            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            orderNumber = daysList.size() + 1;
            if (orderNumber >= 8) {
                LOGGER.warn("[WEB] Cannot show day creation form: all days for week {} are created already", weekId);
                return "redirect:/weeks/" + weekId;
            }

            if (orderNumber == 1) {
                name = "Понедельник";
            } else if (orderNumber == 2) {
                name = "Вторник";
            } else if (orderNumber == 3) {
                name = "Среда";
            } else if (orderNumber == 4) {
                name = "Четверг";
            } else if (orderNumber == 5) {
                name = "Пятница";
            } else if (orderNumber == 6) {
                name = "Суббота";
            } else {
                name = "Воскресенье";
            }
        }

        // Prepare entity with default values
        Day day = new Day();
        day.setDayId(-1);
        day.setProfileId(profileId);
        day.setWeekId(weekId);
        day.setName(name);
        day.setOrderNumber(orderNumber);

        model.addAttribute("day", day);

        return "day_create";
    }

    @PostMapping("/create")
    public String acceptDayCreationForm(@Valid Day day, BindingResult result) {
        LOGGER.trace("[WEB] POST /days/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day creation form: anonymous users cannot create days");
            return "redirect:/profiles";
        }

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' cannot create days", userName);
            return "redirect:/profiles";
        }

        // Validate that provided data is correct
        String dayName = day.getName();
        if (dayName == null || dayName.isEmpty()) {
            FieldError fieldError = new FieldError("day", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "day_create";
        }

        if (dayName.length() > 20) {
            FieldError fieldError = new FieldError("day", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "day_create";
        }

        Integer profileId = day.getProfileId();
        Integer weekId = day.getWeekId();

        if (profileId == null) {
            LOGGER.warn("[WEB] Profile is not provided for day creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Profile {} was not found", profileId);
            return "redirect:/profiles";
        }

        if (!controlService.canReadProfile(userName, profileId)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' has no access to profile {}", userName, profileId);
            return "redirect:/profiles";
        }

        int profileTypeId;
        if (weekId != null) {
            // Split by weeks
            Week week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot accept day creation form: week {} was not found", weekId);
                return "redirect:/profiles/" + profile.getProfileId();
            }
            day.setWeekId(weekId);

            profileTypeId = Constants.ProfileType.DAYS_GROUPED_BY_WEEKS;

            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            int orderNumber = daysList.size() + 1;

            if (orderNumber >= 8) {
                FieldError fieldError = new FieldError("day", "name", "В неделе не может быть больше 7 дней!");
                result.addError(fieldError);
                return "day_create";
            }

            day.setOrderNumber(orderNumber);

        } else {
            // Split by days
            profileTypeId = Constants.ProfileType.DAYS_WITHOUT_GROUPING;

            List<Day> daysList = daysRepository.getAllDaysFromProfile(profileId);
            int orderNumber = daysList.size() + 1;
            day.setOrderNumber(orderNumber);

        }

        // Generate primary key for new entity
        Integer dayId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        day.setDayId(dayId);

        // Create entity
        Day createdDay = daysRepository.addDay(
                day.getDayId(),
                day.getProfileId(),
                day.getWeekId(),
                day.getName(),
                day.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerDayCreated(userName, createdDay);

        // Check and update profile if necessary
        if (Constants.ProfileType.UNDEFINED.equals(profile.getProfileTypeId())) {
            Profile oldData = profilesRepository.getProfile(profileId);

            profile.setProfileTypeId(profileTypeId);
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
