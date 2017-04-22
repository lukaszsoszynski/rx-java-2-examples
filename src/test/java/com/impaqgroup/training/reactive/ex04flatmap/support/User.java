package com.impaqgroup.training.reactive.ex04flatmap.support;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class User {

    private Long id;

    private String name;

    public User(Long id){
        this.id = id;
        this.name = String.format("User name %d", id);
    }
}
