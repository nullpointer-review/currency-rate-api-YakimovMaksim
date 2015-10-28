package test.exchangerate.model;

import java.util.Date;

public class ExchangeRateResponse {
    private final String code;
    private final float rate;
    private final String date;

    public ExchangeRateResponse(String code, float rate, String date) {
        this.code = code;
        this.rate = rate;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public float getRate() {
        return rate;
    }

    public String getDate() {
        return date;
    }
}
