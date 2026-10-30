package com.epam.course.homework.controller;

import java.math.BigDecimal;

public record ProductRequest(String name, String description, BigDecimal price, int quantity) {
}
