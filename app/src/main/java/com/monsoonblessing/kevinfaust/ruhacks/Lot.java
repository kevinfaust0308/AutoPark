package com.monsoonblessing.kevinfaust.ruhacks;

/**
 * Created by Kevin Faust on 3/18/2017.
 */

public class Lot {

    private long lotNumber;
    private int availableSpots;
    private int maxSpots;
    private int maxTime;
    private double hourlyCharge;

    public Lot() {
    }

    public long getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(long lotNumber) {
        this.lotNumber = lotNumber;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(int availableSpots) {
        this.availableSpots = availableSpots;
    }

    public int getMaxSpots() {
        return maxSpots;
    }

    public void setMaxSpots(int maxSpots) {
        this.maxSpots = maxSpots;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public double getHourlyCharge() {
        return hourlyCharge;
    }

    public void setHourlyCharge(double hourlyCharge) {
        this.hourlyCharge = hourlyCharge;
    }

    public void decreaseAvailableSpots() {
        this.availableSpots -= 1;
    }

    public void increaseAvailableSpots() {
        this.availableSpots += 1;
    }


}
