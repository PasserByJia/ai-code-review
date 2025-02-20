package plus.gaga.middleware.utils;

import com.alibaba.fastjson2.JSON;
import plus.gaga.middleware.domain.Message;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WeiXin {
    private final String appid;

    private final String secret;

    private final String touser;

    private final String template_id;

    public WeiXin(String appid, String secret, String touser, String template_id) {
        this.appid = appid;
        this.secret = secret;
        this.touser = touser;
        this.template_id = template_id;
    }

    public  void pushMessage(String logUrl,GitCommand gitCommand) throws Exception {
        String accessToken = WXAccessTokenUtils.getAccessToken(appid,secret);
        Message message = new Message(touser, template_id);
        message.put("project", "big-market");
        message.put("review", logUrl);
        message.put(Message.TemplateKey.REPO_NAME.getCode(), gitCommand.getProject());
        message.put(Message.TemplateKey.BRANCH_NAME.getCode(), gitCommand.getBranch());
        message.put(Message.TemplateKey.COMMIT_AUTHOR.getCode(), gitCommand.getAuthor());
        message.put(Message.TemplateKey.COMMIT_MESSAGE.getCode(), gitCommand.getMessage());
        message.setUrl(logUrl);
        try {
            System.out.println(JSON.toJSONString(message));
            String response = HttpClient.post(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken),
                    JSON.toJSONString(message));
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
