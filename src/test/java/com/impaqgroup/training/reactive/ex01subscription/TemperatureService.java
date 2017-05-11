package com.impaqgroup.training.reactive.ex01subscription;

import java.util.*;

import io.reactivex.Observable;

public class TemperatureService {

    public static final String KALISZ = "Kalisz";
    public static final String POZNAŃ = "Poznań";
    public static final String WARSZAWA = "Warszawa";
    public static final String ŁÓDŹ = "Łódź";
    public static final String LUBLIN = "Lublin";
    public static final String BERLIN = "Berlin";

    public final static Set<String> SUPPORTED_CITIES = new HashSet<>(Arrays.asList(KALISZ, POZNAŃ, WARSZAWA, ŁÓDŹ, LUBLIN));


    public Observable<Double> getTemperature(String cityName){
        if(SUPPORTED_CITIES.contains(cityName)) {
            return Observable.just(17.2, 15.0, 23.0);
        }else if(BERLIN.equalsIgnoreCase(cityName)){
            return Observable.error(new IllegalStateException("Cannot connect to Berlin weather server"));
        }
        return Observable.empty();
    }

}
