package com.monsoonblessing.kevinfaust.ruhacks;

/**
 * Created by Kevin Faust on 3/18/2017.
 */

public class Lot {

    private int lotNumber;
    private int availableSpots;
    private int maxSpots;
    private double hourlyCharge;

    public Lot() {
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(int lotNumber) {
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
