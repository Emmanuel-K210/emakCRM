package com.emak.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@Slf4j
public class UtilisateurController {

	
	@GetMapping(path="/utilisateurs")
	public String statistiques(Model model) {
		model.addAttribute("pageActive","utilisateurs");
		
		return "pages/utilisateurs/utilisateurs.html";
	}
}
