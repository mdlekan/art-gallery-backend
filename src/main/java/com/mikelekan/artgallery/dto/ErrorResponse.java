package com.mikelekan.artgallery.dto;

import java.util.Map;

public record ErrorResponse(int status, String message, long timestamp, Map<String, String> errors) {
}
