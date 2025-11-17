package com.emak.crm.dto;

import java.time.LocalDateTime;

import com.emak.crm.enums.StatutEnvoi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvoiMinimalDTO {
    private Long id;
    private String objet;
    private LocalDateTime dateEnvoi;
    private StatutEnvoi statut; // Supposons que vous avez cet enum
}