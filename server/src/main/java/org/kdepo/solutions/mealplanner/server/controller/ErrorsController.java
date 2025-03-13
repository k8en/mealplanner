package org.kdepo.solutions.mealplanner.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorsController {

    @GetMapping("/business_error")
    public String showBusinessErrorPage() {
        System.out.println("[WEB]" + " GET " + "/business_error");

        return "business_error";
    }
}
