package com.github.thundax.bacon.upms.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import lombok.Getter;

@Getter
public class UserPageQuery extends PageQuery {

    private final String account;
    private final String name;
    private final String phone;
    private final UserStatus status;

    public UserPageQuery(String account, String name, String phone, UserStatus status, Integer pageNo, Integer pageSize) {
        super(pageNo, pageSize);
        this.account = account;
        this.name = name;
        this.phone = phone;
        this.status = status;
    }
}
