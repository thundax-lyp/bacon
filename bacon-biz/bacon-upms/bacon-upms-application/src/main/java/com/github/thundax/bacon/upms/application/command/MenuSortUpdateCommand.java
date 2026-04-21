package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;

public record MenuSortUpdateCommand(MenuId menuId, Integer sort) {}
