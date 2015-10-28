package test.exchangerate.provider;

import org.springframework.stereotype.Controller;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;


class ExchangeRatesHandler extends DefaultHandler {
    private boolean exchangeRateFound;
    private float resultRate;
    private String curCode;
    private float curRate;
    private String curElement;
    private final String currency;

    ExchangeRatesHandler(String currency) {
        this.currency = currency;
    }

    public float getResultRate() {
        return resultRate;
    }

    public boolean isExchangeRateFound() {
        return exchangeRateFound;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        curElement = qName;
        if ("Valute".equals(curElement) && currency.equalsIgnoreCase(curCode)) {
            exchangeRateFound = true;
            resultRate = curRate;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if ("Value".equals(curElement) && length > 1) {
            try {
                curRate = Float.parseFloat(new String(ch, start, length).replace(',', '.'));
            } catch (NumberFormatException nfe) {
                curRate = 0.0f;
            }
        } else if ("CharCode".equals(curElement) && length > 1) {
            curCode = new String(ch, start, length);
        }
    }
}

@Controller
public class CDRFNoCacheExchangeRateProvider implements ExchangeRateProvider {
    private final static Logger logger = Logger.getLogger("exch-rate-logger");

    private final static DateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final static DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private final static String SPECIFIC_DATE_SERVICE_URL = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=%s";
    private final static String CURRENT_DATE_SERVICE_URL = "http://www.cbr.ru/scripts/XML_daily.asp";

    private SAXParserFactory factory = SAXParserFactory.newInstance();

    private String getExchangeRates(String date) throws Exception {
        URL url;
        HttpURLConnection con;
        StringBuilder response = new StringBuilder();
        BufferedReader in = null;

        String serviceUrl;

        if (date != null) {
            try {
                Date d = INPUT_DATE_FORMAT.parse(date);
                serviceUrl = String.format(SPECIFIC_DATE_SERVICE_URL, OUTPUT_DATE_FORMAT.format(d));
            } catch (ParseException e) {
                serviceUrl = CURRENT_DATE_SERVICE_URL;
            }
        } else {
            serviceUrl = CURRENT_DATE_SERVICE_URL;
        }

        try {
            url = new URL(serviceUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (HttpURLConnection.HTTP_OK != responseCode) {
                throw new Exception(String.format("Bad response code: %d", responseCode));
            }

            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }  finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }
            }
        }

        return response.toString();
    }

    @Override
    public float getRate(String currency, String date) throws Exception {
        String rawExchangeRates = getExchangeRates(date);
        ExchangeRatesHandler handler = new ExchangeRatesHandler(currency);
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(new StringReader(rawExchangeRates)), handler);
        if (handler.isExchangeRateFound()) {
            return handler.getResultRate();
        } else {
            throw new Exception(String.format("No exchange rate found for currency <%s>, date <%s>", currency, date));
        }
    }
}
