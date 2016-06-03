package ru.ekaerovets.service;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

/**
 * @author karyakin dmitry
 *         date 31.10.15.
 */
@Component
public class PinyinFetcher {

    private static final Logger logger = LoggerFactory.getLogger(PinyinFetcher.class);

    public String fetch(String input) {
        try {
            HttpClient client = new HttpClient();
            String encoded = URLEncoder.encode(input, "UTF-8");
            GetMethod method = new GetMethod("http://popupchinese.com/adso/pinyin.php?text=" + encoded);
            client.executeMethod(method);
            String res = method.getResponseBodyAsString();
            String unescaped = StringEscapeUtils.unescapeHtml4(res);
            int index1 = unescaped.indexOf("annotation = '");
            int index2 = unescaped.indexOf(" ';update_");
            if (index1 == -1 || index2 == -1 || index1 + 14 >= index2) {
                return "?";
            }
            return unescaped.substring(index1 + 14, index2);
        } catch (Exception e) {
            logger.warn("Cannot fetch pinyin for input " + input, e);
            return "?";
        }
    }

}
