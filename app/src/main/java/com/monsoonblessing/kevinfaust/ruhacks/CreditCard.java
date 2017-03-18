package com.monsoonblessing.kevinfaust.ruhacks;

/**
 * Created by Kevin Faust on 3/18/2017.
 */

public class CreditCard {
    private int cardNumber;
    private String cardName;
    private int cardSecurityNumber;
    private String cardExpiry; // ex: 06/22 (mm/yy)

    public CreditCard() {
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public int getCardSecurityNumber() {
        return cardSecurityNumber;
    }

    public void setCardSecurityNumber(int cardSecurityNumber) {
        this.cardSecurityNumber = cardSecurityNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }
}
