package com.shubham.Expense.Tracker.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shubham.Expense.Tracker.entities.UserInfo;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserInfoDto extends UserInfo {
    private String firstName;
    private String lastName;
    private Long phoneNumber;
    private String email;
}
