package com.emak.crm.dto;

import java.time.LocalDateTime;

public record AlerteResponse(
    String type,
    String message,
    String priorite,
    LocalDateTime dateCreation,
    Long entiteId,
    String entiteType
) {}