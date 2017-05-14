package com.impaqgroup.training.reactive.in03callback.model;

import lombok.Value;

@Value
public class PersonPosition {
    private Person person;
    private Coordinates coordinates;
}
