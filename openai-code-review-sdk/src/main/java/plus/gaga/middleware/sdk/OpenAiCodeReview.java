package plus.gaga.middleware.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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
        System.out.println("code review：" + log);
    }

    public static String deepseek (String code) throws Exception {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时时间
                .writeTimeout(30, TimeUnit.SECONDS)  // 写入超时时间
                .readTimeout(30, TimeUnit.SECONDS)   // 读取超时时间
                .build();

        // 构建请求体
        String jsonBody = "{"
                + "  \"model\": \"deepseek-chat\","
                + "  \"messages\": ["
                + "    {\"role\": \"user\", \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:\"},"
                + "    {\"role\": \"user\", \"content\": \""+code+"\"}"
                + "  ]"
                + "}";

        // 构建请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // 打印响应内容
                //System.out.println("Response: " + response.body().string());
                String jsonResponse = response.body().string();

                // 使用 FastJSON 解析 JSON 数据
                JSONObject jsonObject = JSON.parseObject(jsonResponse);

                // 获取 "choices" 数组
                JSONArray choices = jsonObject.getJSONArray("choices");

                // 获取第一个元素的 "message" 对象
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");

                // 提取 "content" 字段
                String content = message.getString("content");
                return content;
                //System.out.println("Response: " + content);
            } else {
                System.out.println("Request failed: " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  "failed";
    }
}

