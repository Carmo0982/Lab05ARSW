package edu.eci.arsw.blueprints.controllers.response;

public record ApiResponse<T>(int code, String message, T data) { }