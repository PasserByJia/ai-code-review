package plus.gaga.middleware.utils;

import com.alibaba.fastjson2.JSON;
import plus.gaga.middleware.domain.ChatCompletionRequest;
import plus.gaga.middleware.domain.ChatCompletionSyncResponse;
import plus.gaga.middleware.domain.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AI {

    private final String apiHost;
    private final String apiKeySecret;

    public AI(String apiHost, String apiKeySecret) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKeySecret;
    }

    public String codeReview(ArrayList<ChatCompletionRequest.Prompt> prompts) throws Exception {
        String token = BearerTokenUtils.getToken(apiKeySecret);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(prompts);

        String content = HttpClient.post(apiHost,token,JSON.toJSONString(chatCompletionRequest));

        System.out.println("评审结果：" + content.toString());

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();
    }
}
