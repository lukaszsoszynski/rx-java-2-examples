package com.impaqgroup.training.reactive.ex04flatmap;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import com.impaqgroup.training.reactive.ex04flatmap.support.PhoneNumber;
import com.impaqgroup.training.reactive.ex04flatmap.support.User;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex03FlatMapObservableTest {

    private static final Long DEPARTMENT_ID = 4342L;
    private CostReactiveService costService;

    @Before
    public void setUp(){
        this.costService = new CostReactiveService();
    }

    /*
     * Task: find total cost of sending SMS for all phones belongs for all user from given department
     */

    @Test
    public void onlyWithMap(){
        Observable<Observable<PhoneNumber>> usersPhonesNumbers = costService
                .findUserInDepartment(DEPARTMENT_ID)
                .map(costService::findPhoneNumber);//<-- returns observable

        //Strange type here :o
        Observable<Observable<BigDecimal>> priceStream = usersPhonesNumbers
                .map(phones -> phones.map(costService::getPricePerSmsForNumber));

        Observable<BigDecimal> costPerPerson = priceStream.map(userPhones -> userPhones.reduce(ZERO, BigDecimal::add).blockingGet());//blockingGet<---only one difference in comparison to streams

        BigDecimal totalCost = costPerPerson.reduce(ZERO, BigDecimal::add).blockingGet(); //blockingGet<---only one difference in comparison to streams
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

    @Test
    public void withFlatMap(){
        Observable<BigDecimal> pricePerSms = costService.findUserInDepartment(DEPARTMENT_ID)
                .flatMap(costService::findPhoneNumber)
                .map(costService::getPricePerSmsForNumber);

        BigDecimal totalCost = pricePerSms.reduce(ZERO, BigDecimal::add).blockingGet();
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

    @Test
    public void flatMapWithOtherTypes(){
        Observable<BigDecimal> pricePerSms = costService.findUserInDepartment(DEPARTMENT_ID)
                .flatMapIterable(this::findPhoneNumber) //<-- this method returns List
                //^ a few additional flatMapXxx methods exists
                .map(costService::getPricePerSmsForNumber);

        BigDecimal totalCost = pricePerSms.reduce(ZERO, BigDecimal::add).blockingGet();
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

    private List<PhoneNumber> findPhoneNumber(User user){
        PhoneNumber phoneNumber1 = new PhoneNumber(user.getId() * 3 + 2, "mobile", String.format("%d", 800000000L + user.getId()));
        PhoneNumber phoneNumber2 = new PhoneNumber(user.getId() * 3 + 3, "mobile", String.format("%d", 80000000L + user.getId()));
        return Arrays.asList(phoneNumber1, phoneNumber2);
    }

    @Test
    public void extendedFlatMapFallback(){
        Observable<String> error = Observable.error(new IllegalStateException("Something went wrong..."));
        Observable<String> fallback = Observable.just("Fallback One", "Fallback Two");
        Observable<String> afterComplete = Observable.just("Three");

        error
                .flatMap(event -> Observable.just(event), exception -> fallback, () -> afterComplete)//<-- flat map for error is only executed
                .subscribe(log::info, ex -> log.info("Got error", ex), () -> log.info("On complete"));
        //three is not display because error mapping occurred
    }

    @Test
    public void extendedFlatMapOnComplete(){
        Observable<String> observable = Observable.just("One");
        Observable<String> fallback = Observable.just("Fallback Two");
        Observable<String> afterComplete = Observable.just("After complete Three");

        observable
                .flatMap(event -> Observable.just("Extended " + event), exception -> fallback, () -> afterComplete)//flat map for event and on complete is executed
                .subscribe(log::info, ex -> log.info("Got error", ex), () -> log.info("On complete"));
        //"Two" is not display because onComplete mapping occurred
    }

    @Test
    public void flatMapIsConcurrentByDefault(){
        Observable<BigDecimal> pricePerSms = costService.findUserInDepartment(DEPARTMENT_ID)// get 3 users
                //flat map subscribe to all Observable returned from slowFindPhoneNumber simultaneous!
                .flatMap(costService::slowFindPhoneNumber)//<-- this is "slow", event is emitted after 3 sec.
                .map(costService::getPricePerSmsForNumber);

        log.info("Soon before subscribe");
        //this last about 3 sec. due to concurrency
        BigDecimal totalCost = pricePerSms.reduce(ZERO, BigDecimal::add).blockingGet();
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }

    @Test
    @Ignore
    public void avoidConcurrentFlatMap(){
        Observable<BigDecimal> pricePerSms = costService.findUserInDepartment(DEPARTMENT_ID)// get 3 users
                //concurrency is limited, only ONE subscription simultaneous
                .flatMap(costService::slowFindPhoneNumber, 1)//<-- this is "slow", event is emitted after 3 sec.
                .map(costService::getPricePerSmsForNumber);

        log.info("Soon before subscribe");
        //this last about 9 sec. due to lack of concurrency
        BigDecimal totalCost = pricePerSms.reduce(ZERO, BigDecimal::add).blockingGet();
        log.info("Total cost of sending SMSes to all users all phones {}", totalCost);
    }
}

class CostReactiveService{

    public Observable<User> findUserInDepartment(Long departmentId){
        return Observable.just(new User(departmentId), new User(departmentId + 1), new User(departmentId + 2));
    }


    public Observable<PhoneNumber> findPhoneNumber(User user){
        PhoneNumber phoneNumber1 = new PhoneNumber(user.getId() * 3 + 2, "mobile", String.format("%d", 800000000L + user.getId()));
        PhoneNumber phoneNumber2 = new PhoneNumber(user.getId() * 3 + 3, "mobile", String.format("%d", 80000000L + user.getId()));
        return Observable.just(phoneNumber1, phoneNumber2);
    }

    public Observable<PhoneNumber> slowFindPhoneNumber(User user) {
        //Return from this method instantaneous,
        return findPhoneNumber(user)
                .delay(3, TimeUnit.SECONDS);//but each event is postponed about 3 sec.
    }

    /**
     * @return return NOT null value
     */
    public BigDecimal getPricePerSmsForNumber(PhoneNumber phoneNumber){
        return BigDecimal.valueOf(phoneNumber.getId()).divide(BigDecimal.valueOf(100));
    }
}