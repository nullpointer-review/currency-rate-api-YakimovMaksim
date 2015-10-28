package test.exchangerate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import test.exchangerate.model.ExchangeRateResponse;
import test.exchangerate.provider.ExchangeRateProvider;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/rate")
public class ApiController {
    private final static Logger logger = Logger.getLogger("exch-rate-logger");

    @Autowired
    private ExchangeRateProvider exchangeRateProvider;

    @RequestMapping(value = "/{code}/{date}", method = RequestMethod.GET, produces="application/json")
    public ExchangeRateResponse getExchangeRate(@PathVariable String code, @PathVariable String date) throws Exception {
        try {
            float rate = exchangeRateProvider.getRate(code, date);
            return new ExchangeRateResponse(code, rate, date);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new Exception(String.format("No exchange rate found for currency <%s>, date <%s>", code, date));
        }
    }


    @ExceptionHandler
    void handleIllegalArgumentException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
}
