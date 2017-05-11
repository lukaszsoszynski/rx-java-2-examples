package com.impaqgroup.training.reactive.ex04flatmap;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.impaqgroup.training.reactive.ex04flatmap.support.PhoneNumber;
import com.impaqgroup.training.reactive.ex04flatmap.support.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex01FlatMapOptionalTest {

    public static final long USER_ID = 12643L;
    private OptionalService optionalService;
    private Service service;

    @Before
    public void setUp(){
        //this service return null when if sth is not found
        this.service = new Service();
        this.optionalService = new OptionalService();
    }

    @Test
    public void simpleMapWithNullableService(){
        //TASK get price of phone call to the user, using service
        Optional<BigDecimal> price = Optional
                .ofNullable(service.findUser(USER_ID))//<-- ugly ofNullable invocation
                .map(service::findPhoneNumber)
                .map(service::getPricePerMinuteForNumber);

        price.ifPresent(value -> log.info("Price for phone call to user is {}", value));
    }

    @Test
    public void shouldGetUserCostOfPhoneCallToUser(){
        Optional<User> user = optionalService.findUser(USER_ID);

        //is this type strange?
        Optional<Optional<PhoneNumber>> phoneNumber = user.map(optionalService::findPhoneNumber);

        //this is strange for sure
        Optional<Optional<Optional<BigDecimal>>> price = phoneNumber.map(phoneNumberOptional -> phoneNumberOptional.map(optionalService::getPricePerMinuteForNumber));

        //how to get a price?
        price.ifPresent(optionalNumber -> optionalNumber.ifPresent(priceOptional -> priceOptional.ifPresent(value -> log.info("Price: {}", value))));
    }

    @Test
    public void easyWayOfGettingPriceOfPhoneCallToUser(){
        Optional<BigDecimal> price = optionalService
                .findUser(USER_ID)
                .flatMap(optionalService::findPhoneNumber)
                .flatMap(optionalService::getPricePerMinuteForNumber);

        //how to get price
        price.ifPresent(value -> log.info("Price of phone call to user is {}.", value));
    }

}

class Service{

    /**
     * @return null if user not found
     */
    public User findUser(Long userId){
        return new User(userId);
    }

    /**
     * @return return null if user has no phone number
     */
    public PhoneNumber findPhoneNumber(User user){
        return new PhoneNumber(user.getId() * 3 + 2, "mobile", String.format("%d", 800000000L + user.getId()));
    }

    /**
     * @return return null if price is unknown
     */
    public BigDecimal getPricePerMinuteForNumber(PhoneNumber phoneNumber){
        return BigDecimal.valueOf(phoneNumber.getId()).divide(BigDecimal.valueOf(100));
    }

}

class OptionalService {

    private Service service = new Service();

    public Optional<User> findUser(Long userId){
        return Optional.of(service.findUser(userId));
    }

    public Optional<PhoneNumber> findPhoneNumber(User user){
        return Optional.of(service.findPhoneNumber(user));
    }

    public Optional<BigDecimal> getPricePerMinuteForNumber(PhoneNumber phoneNumber){
        return Optional.of(service.getPricePerMinuteForNumber(phoneNumber));
    }
}
