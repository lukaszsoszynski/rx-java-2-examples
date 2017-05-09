package com.impaqgroup.training.reactive.in00callback.model;

import lombok.Value;

@Value
public class PersonPosition {
    private Person person;
    private Coordinates coordinates;
}
