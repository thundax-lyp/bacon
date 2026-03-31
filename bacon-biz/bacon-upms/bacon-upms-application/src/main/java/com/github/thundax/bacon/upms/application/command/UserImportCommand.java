package com.github.thundax.bacon.upms.application.command;

public record UserImportCommand(String account, String name, String phone, String departmentId) {
}
