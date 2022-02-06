package net.vendingmachine.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-security-example
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 9/28/17
 * Time: 7:01 AM
 * To change this template use File | Settings | File Templates.
 */

public enum Deposit {
    FIVE("FIVE",5),
    TEN("TEN",10),
    TWENTY("TWENTY",20),
    FIFTY("FIFTY",50),
    HUNDRED("HUNDRED",100);

    private long amount;
    private String value;

    Deposit(String value,long amount) {
        this.value = value;
        this.amount = amount;
    }

    public static boolean findByAmount(long amount) {
        boolean checkValue = false;
        Optional<Deposit> dep = Arrays.stream(values()).filter(elem -> elem.amount == amount).findFirst();
        if(dep.isPresent())
            checkValue = true;
        return  checkValue;
    }
}
