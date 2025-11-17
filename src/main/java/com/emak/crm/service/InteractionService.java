package com.emak.crm.service;

import org.springframework.stereotype.Service;

import com.emak.crm.dto.InteractionRequest;
import com.emak.crm.dto.InteractionResponse;
@Service
public interface InteractionService extends CrudService<InteractionRequest, InteractionResponse>{

}
