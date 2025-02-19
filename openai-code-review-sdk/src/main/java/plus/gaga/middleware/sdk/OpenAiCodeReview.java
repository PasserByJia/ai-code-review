package plus.gaga.middleware.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;


public class OpenAiCodeReview {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String API_KEY = "sk-46e55fc6f58042698bbfdf0139cc0cdb"; // 替换为你的 OpenAI API Key

    public static void main(String[] args) throws Exception {
        System.out.println("测试执行");
        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);

        System.out.println("评审代码：" + diffCode.toString());
        String log = deepseek(diffCode.toString());
        //String log = deepseek("diffCode.toString()");
        System.out.println("code review：" + log);
    }

    public static String deepseek (String code) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        // 构建请求体
        String jsonBody = "{"
                + "  \"model\": \"deepseek-chat\","
                + "  \"messages\": ["
                + "    {\"role\": \"user\", \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:\"},"
                + "    {\"role\": \"user\", \"content\": \""+code+"\"}"
                + "  ]"
                + "}";

        // 将请求体写入输出流
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 获取响应码
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // 读取响应内容
        Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
        String responseBody = scanner.useDelimiter("\\A").next();

        // 打印响应内容
        System.out.println("Response Body: " + responseBody);

        JSONObject jsonObject = JSON.parseObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        String content = message.getString("content");

        return content;
    }
}

