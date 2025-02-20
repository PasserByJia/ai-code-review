package plus.gaga.middleware.utils;

import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WXAccessTokenUtils {

    private static final String APPID = "wx5fd63cf6f7d30c39";
    private static final String SECRET = "a8a4c25fb71671bdf5a64fa2b1b632f5";
    private static final String GRANT_TYPE = "client_credential";
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";

    public static String getAccessToken(String APPID, String SECRET) throws Exception {
        String response = HttpClient.get(String.format(URL_TEMPLATE, GRANT_TYPE, APPID, SECRET));
        Token token = JSON.parseObject(response, Token.class);
        if (token != null) {
            return token.getAccess_token();
        }else {
            return null;
        }
    }

    public static class Token {
        private String access_token;
        private Integer expires_in;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }


}
