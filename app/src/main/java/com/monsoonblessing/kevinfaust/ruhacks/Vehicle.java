package com.monsoonblessing.kevinfaust.ruhacks;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Kevin Faust on 3/18/2017.
 */

@DynamoDBTable(tableName = "Vehicle")
public class Vehicle {

    private String plateNumber;
    private int creditCard;
    private long timeIn;
    private Long timeOut;


    @DynamoDBAttribute(attributeName = "TimeIn")
    public long getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(long timeIn) {
        this.timeIn = timeIn;
    }

    @DynamoDBAttribute(attributeName = "CreditCard")
    public int getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(int creditCard) {
        this.creditCard = creditCard;
    }

    @DynamoDBAttribute(attributeName = "TimeOut")
    public Long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Long timeOut) {
        this.timeOut = timeOut;
    }

    @DynamoDBHashKey(attributeName = "PlateNumber")
    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

}

