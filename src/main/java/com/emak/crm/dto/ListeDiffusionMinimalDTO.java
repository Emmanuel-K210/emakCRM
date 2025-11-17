package com.emak.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeDiffusionMinimalDTO {
    private Long id;
    private String nomListe;
    private String description;
    private Integer nombreContacts;
}
