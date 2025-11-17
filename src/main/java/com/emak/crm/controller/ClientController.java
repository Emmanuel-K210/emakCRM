package com.emak.crm.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emak.crm.dto.ClientRequest;
import com.emak.crm.enums.OrigineClient;
import com.emak.crm.enums.TypeClient;
import com.emak.crm.service.ClientService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@RequestMapping("/clients")
public class ClientController{
	private ClientService clientService;
	
	@GetMapping
	public String clients(Model model,
			@RequestParam(name="size",defaultValue = "5")int size,
			@RequestParam(name="page",defaultValue = "0") int page
			) {
		model.addAttribute(null, clientService.findAll(PageRequest.of(page, page)));
		model.addAttribute("pageActive", "clients");
		return "pages/clients/clients";
	}
	
	@GetMapping(path="/ajouter")
	public String ajouterClient(Model model) {
		model.addAttribute("pageActive","clients");
		model.addAttribute("typeClient",TypeClient.values());
		model.addAttribute("origineClient", OrigineClient.values());
		return "pages/clients/nouveau.html";
	}
	
	@PostMapping("/ajouter")
	public String posterClient(ClientRequest request) {
		var client = clientService.save(request);
		return "pages/clients/felication.html";
	}
}