package com.impaqgroup.training.reactive.other;

import java.util.List;

import io.reactivex.Observable;

public class LegancyAndReactiveService {

    interface LegacyService{

        List<Order> findPendingOrder();

        List<Order> findOrderReadyToShipping();

        List<Item> listOrderItem(Order order);
    }

    interface ReactiveService{

        Observable<Order> findPendingOrder();

        Observable<Order> findOrderReadyToShipping();

        Observable<Item> listOrderItem(Order order);
    }

    private static class Order{}

    private static class Item{}

}
