package test.exchangerate.provider;

public interface ExchangeRateProvider {
    float getRate(String currency, String date) throws Exception;
}
