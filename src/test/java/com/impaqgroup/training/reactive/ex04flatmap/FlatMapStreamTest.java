package com.impaqgroup.training.reactive.ex04flatmap;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.impaqgroup.training.reactive.ex04flatmap.support.PhoneNumber;
import com.impaqgroup.training.reactive.ex04flatmap.support.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlatMapStreamTest {

    public static final long DEPARTMENT_ID = 23321L;
    private CostService costService;

    @Before
    public void setUp(){
        this.costService = new CostService();
    }

    /*
     * Task: find total cost of sending SMS for all phones belongs for all user from given department
     */

    @Test
    public void onlyWithMap(){
        Stream<Stream<PhoneNumber>> usersPhonesNumbers = costService
                .findUserInDepartment(DEPARTMENT_ID)
                .map(costService::findPhoneNumber);

        //Strange type here :o
        Stream<Stream<BigDecimal>> priceStream = usersPhonesNumbers
                .map(phones -> phones.map(costService::getPricePerSmsForNumber));

        Stream<BigDecimal> costPerPerson = priceStream.map(userPhones -> userPhones.reduce(ZERO, BigDecimal::add));

        BigDecimal totalCost = costPerPerson.reduce(ZERO, BigDecimal::add);
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

    @Test
    public void withFlatMap(){
        Stream<BigDecimal> pricePerSms = costService.findUserInDepartment(DEPARTMENT_ID)
                .flatMap(costService::findPhoneNumber)
                .map(costService::getPricePerSmsForNumber);

        BigDecimal totalCost = pricePerSms.reduce(ZERO, BigDecimal::add);
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

}

class CostService{

    public Stream<User> findUserInDepartment(Long departmentId){
        return Stream
                .<User>builder()
                .add(new User(departmentId))
                .add(new User(departmentId + 1))
                .add(new User(departmentId + 2))
                .build();
    }


    public Stream<PhoneNumber> findPhoneNumber(User user){
        return Stream.<PhoneNumber>builder()
                .add(new PhoneNumber(user.getId() * 3 + 2, "mobile", String.format("%d", 800000000L + user.getId())))
                .add(new PhoneNumber(user.getId() * 3 + 3, "mobile", String.format("%d", 80000000L + user.getId())))
                .build();

        //return new PhoneNumber(user.getId() * 3 + 2, "mobile", String.format("%d", 800000000L + user.getId()));
    }

    /**
     * @return return NOT null value
     */
    public BigDecimal getPricePerSmsForNumber(PhoneNumber phoneNumber){
        return BigDecimal.valueOf(phoneNumber.getId()).divide(BigDecimal.valueOf(100));
    }

}
