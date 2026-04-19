package com.cburch.logisim.ai.service;


import com.cburch.logisim.ai.config.AiConfig;
import com.cburch.logisim.ai.model.ChatMessage;
import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AiServiceImpl implements AiService
{
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_RETRIES = 2;
    private final OkHttpClient httpClient;
    
    public AiServiceImpl()
    {
        this.httpClient = new OkHttpClient.Builder()
                              .connectTimeout(30, TimeUnit.SECONDS)
                              .readTimeout(2, TimeUnit.MINUTES)
                              .writeTimeout(2, TimeUnit.MINUTES)
                              .callTimeout(3, TimeUnit.MINUTES)
                              .retryOnConnectionFailure(true)
                              .build();
    }
    
    @Override
    public CompletableFuture<String> sendMessage(List<ChatMessage> messages, AiConfig config)
    {
        return sendMessageWithRetry(messages, config, 0);
    }
    
    private CompletableFuture<String> sendMessageWithRetry(List<ChatMessage> messages, AiConfig config, int retryCount)
    {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try
        {
            String jsonBody = buildRequestBody(messages, config);
            
            System.out.println("========================================");
            System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] 发送请求到 AI 模型");
            System.out.println("模型类型: " + config.getModelType());
            System.out.println("API URL: " + config.getApiUrl());
            if (retryCount > 0)
            {
                System.out.println("重试次数: " + retryCount + "/" + MAX_RETRIES);
            }
            System.out.println("请求内容:");
            System.out.println(jsonBody);
            System.out.println("========================================");
            
            RequestBody body = RequestBody.create(jsonBody, JSON);
            
            Request.Builder requestBuilder = new Request.Builder()
                                                 .url(config.getApiUrl())
                                                 .post(body)
                                                 .addHeader("Content-Type", "application/json");
            
            addAuthHeader(requestBuilder, config);
            
            Request request = requestBuilder.build();
            
            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    String errorMessage = analyzeNetworkError(e);
                    
                    System.err.println("========================================");
                    System.err.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] AI 请求失败");
                    System.err.println("错误类型: " + e.getClass().getSimpleName());
                    System.err.println("错误信息: " + errorMessage);
                    System.err.println("详细原因: " + e.getMessage());
                    System.err.println("========================================");
                    
                    if (retryCount < MAX_RETRIES && shouldRetry(e))
                    {
                        System.out.println("将在 3 秒后重试...");
                        try
                        {
                            Thread.sleep(3000);
                        } catch (InterruptedException ie)
                        {
                            Thread.currentThread().interrupt();
                            future.completeExceptionally(ie);
                            return;
                        }
                        
                        sendMessageWithRetry(messages, config, retryCount + 1)
                            .thenAccept(future::complete)
                            .exceptionally(ex ->
                            {
                                future.completeExceptionally(ex);
                                return null;
                            });
                    } else
                    {
                        provideNetworkTroubleshootingTips(e);
                        future.completeExceptionally(new IOException(errorMessage, e));
                    }
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    if (!response.isSuccessful())
                    {
                        String errorMsg = "Request failed: " + response.code();
                        String responseBody = "";
                        try
                        {
                            if (response.body() != null)
                            {
                                responseBody = response.body().string();
                            }
                        } catch (Exception ex)
                        {
                        }
                        
                        System.err.println("========================================");
                        System.err.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] AI 响应错误");
                        System.err.println("HTTP 状态码: " + response.code());
                        System.err.println("响应内容: " + responseBody);
                        System.err.println("========================================");
                        future.completeExceptionally(new IOException(errorMsg));
                        return;
                    }
                    
                    try
                    {
                        String responseBody = response.body().string();
                        
                        System.out.println("========================================");
                        System.out.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] 收到 AI 响应");
                        System.out.println("响应内容:");
                        System.out.println(responseBody);
                        System.out.println("========================================");
                        
                        String result = parseResponse(responseBody, config);
                        future.complete(result);
                    } catch (Exception e)
                    {
                        System.err.println("========================================");
                        System.err.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] 解析 AI 响应失败");
                        System.err.println("错误信息: " + e.getMessage());
                        e.printStackTrace();
                        System.err.println("========================================");
                        future.completeExceptionally(e);
                    }
                }
            });
        } catch (Exception e)
        {
            System.err.println("========================================");
            System.err.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] 创建 AI 请求失败");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private String analyzeNetworkError(IOException e)
    {
        if (e instanceof UnknownHostException)
        {
            return "无法解析域名，请检查网络连接和 API URL 是否正确";
        } else if (e instanceof ConnectException)
        {
            String message = e.getMessage();
            if (message != null && message.contains("Network is unreachable"))
            {
                return "网络不可达，请检查：\n" +
                       "1. 网络连接是否正常\n" +
                       "2. 防火墙是否阻止了连接\n" +
                       "3. 是否需要配置代理\n" +
                       "4. API URL 是否正确";
            }
            return "连接失败: " + message;
        } else if (e instanceof SocketTimeoutException)
        {
            return "请求超时，请检查网络连接或稍后重试";
        } else
        {
            return "网络错误: " + e.getMessage();
        }
    }
    
    private boolean shouldRetry(IOException e)
    {
        if (e instanceof ConnectException || 
            e instanceof SocketTimeoutException ||
            e instanceof UnknownHostException)
        {
            return true;
        }
        
        String message = e.getMessage();
        return message != null && 
               (message.contains("Network is unreachable") || 
                message.contains("timeout") ||
                message.contains("Connection refused"));
    }
    
    private void provideNetworkTroubleshootingTips(IOException e)
    {
        System.err.println("\n💡 网络故障排查建议:");
        System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (e.getMessage() != null && e.getMessage().contains("Network is unreachable"))
        {
            System.err.println("1. 检查网络连接是否正常");
            System.err.println("   - 尝试访问网页确认网络可用");
            System.err.println("   - 检查 WiFi/以太网连接");
            System.err.println("");
            System.err.println("2. 检查防火墙设置");
            System.err.println("   - Windows 防火墙可能阻止了 Java 应用");
            System.err.println("   - 将 Java 添加到防火墙白名单");
            System.err.println("");
            System.err.println("3. 检查代理设置");
            System.err.println("   - 如果使用代理，需要配置 JVM 参数:");
            System.err.println("     -Dhttp.proxyHost=your_proxy_host");
            System.err.println("     -Dhttp.proxyPort=your_proxy_port");
            System.err.println("     -Dhttps.proxyHost=your_proxy_host");
            System.err.println("     -Dhttps.proxyPort=your_proxy_port");
            System.err.println("");
            System.err.println("4. 验证 API URL");
            System.err.println("   - 确认 API URL 格式正确");
            System.err.println("   - 尝试在浏览器中访问 API 端点");
            System.err.println("");
            System.err.println("5. 检查 DNS 设置");
            System.err.println("   - 尝试使用 8.8.8.8 或 114.114.114.114");
        } else
        {
            System.err.println("1. 检查网络连接");
            System.err.println("2. 验证 API 密钥和 URL 配置");
            System.err.println("3. 检查防火墙和代理设置");
            System.err.println("4. 查看控制台详细错误信息");
        }
        
        System.err.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
    
    @Override
    public CompletableFuture<Boolean> testConnection(AiConfig config)
    {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try
        {
            String testMessage = "Say 'Connection successful' in one sentence.";
            ChatMessage msg = new ChatMessage(ChatMessage.Role.USER, testMessage);
            
            System.out.println("\n🔍 开始测试 AI 连接...");
            
            sendMessage(List.of(msg), config).thenAccept(response ->
            {
                System.out.println("✅ 连接测试成功！");
                future.complete(response != null && !response.isEmpty());
            }).exceptionally(ex ->
            {
                System.err.println("❌ 连接测试失败: " + ex.getMessage());
                future.complete(false);
                return null;
            });
        } catch (Exception e)
        {
            System.err.println("❌ 连接测试异常: " + e.getMessage());
            future.complete(false);
        }
        
        return future;
    }
    
    private void addAuthHeader(Request.Builder builder, AiConfig config)
    {
        switch (config.getModelType())
        {
            case QWEN:
                builder.addHeader("Authorization", "Bearer " + config.getApiKey());
                break;
            case GPT:
                builder.addHeader("Authorization", "Bearer " + config.getApiKey());
                break;
            case GEMINI:
                break;
            case CUSTOM:
                builder.addHeader("Authorization", "Bearer " + config.getApiKey());
                break;
        }
    }
    
    private String buildRequestBody(List<ChatMessage> messages, AiConfig config)
    {
        JsonObject json = new JsonObject();
        JsonArray messagesArray = new JsonArray();
        
        for (ChatMessage msg : messages)
        {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole().name().toLowerCase());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        
        switch (config.getModelType())
        {
            case QWEN:
                json.addProperty("model", "qwen-turbo");
                
                JsonObject inputObj = new JsonObject();
                inputObj.add("messages", messagesArray);
                json.add("input", inputObj);
                
                JsonObject paramsObj = new JsonObject();
                paramsObj.addProperty("result_format", "message");
                json.add("parameters", paramsObj);
                break;
                
            case GPT:
                json.addProperty("model", "gpt-3.5-turbo");
                json.add("messages", messagesArray);
                json.addProperty("temperature", 0.7);
                break;
                
            case GEMINI:
                json.add("contents", messagesArray);
                break;
                
            case CUSTOM:
                json.add("messages", messagesArray);
                break;
        }
        
        String requestBody = json.toString();
        
        if (config.getModelType() == AiConfig.ModelType.QWEN)
        {
            System.out.println("\n📤 千问API请求体结构:");
            System.out.println("{");
            System.out.println("  \"model\": \"qwen-turbo\",");
            System.out.println("  \"input\": {");
            System.out.println("    \"messages\": [");
            for (int i = 0; i < messagesArray.size(); i++)
            {
                JsonObject msg = messagesArray.get(i).getAsJsonObject();
                System.out.println("      {\"role\": \"" + msg.get("role").getAsString() + 
                                 "\", \"content\": \"...\"}");
            }
            System.out.println("    ]");
            System.out.println("  },");
            System.out.println("  \"parameters\": {");
            System.out.println("    \"result_format\": \"message\"");
            System.out.println("  }");
            System.out.println("}");
            System.out.println();
        }
        
        return requestBody;
    }
    
    private String parseResponse(String responseBody, AiConfig config)
    {
        try
        {
            System.out.println("\n📋 原始响应内容:");
            System.out.println(responseBody);
            System.out.println("====================\n");
            
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            JsonObject json = jsonElement.getAsJsonObject();
            
            switch (config.getModelType())
            {
                case QWEN:
                    if (json.has("output"))
                    {
                        JsonObject output = json.getAsJsonObject("output");
                        if (output.has("choices"))
                        {
                            JsonArray choices = output.getAsJsonArray("choices");
                            if (choices.size() > 0)
                            {
                                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                                if (firstChoice.has("message"))
                                {
                                    return firstChoice.getAsJsonObject("message")
                                                      .get("content").getAsString();
                                } else if (firstChoice.has("text"))
                                {
                                    return firstChoice.get("text").getAsString();
                                }
                            }
                        } else if (output.has("text"))
                        {
                            return output.get("text").getAsString();
                        }
                    }
                    
                    if (json.has("code") && json.has("message"))
                    {
                        String errorCode = json.get("code").getAsString();
                        String errorMsg = json.get("message").getAsString();
                        throw new RuntimeException("千问 API 错误 [" + errorCode + "]: " + errorMsg);
                    }
                    
                    throw new RuntimeException("无法解析千问响应，响应格式: " + responseBody);
                    
                case GPT:
                    if (json.has("error"))
                    {
                        String errorMsg = json.getAsJsonObject("error")
                                             .get("message").getAsString();
                        throw new RuntimeException("OpenAI API 错误: " + errorMsg);
                    }
                    return json.getAsJsonArray("choices")
                               .get(0).getAsJsonObject()
                               .getAsJsonObject("message")
                               .get("content").getAsString();
                               
                case GEMINI:
                    if (json.has("error"))
                    {
                        String errorMsg = json.getAsJsonObject("error")
                                             .get("message").getAsString();
                        throw new RuntimeException("Gemini API 错误: " + errorMsg);
                    }
                    return json.getAsJsonArray("candidates")
                               .get(0).getAsJsonObject()
                               .getAsJsonObject("content")
                               .getAsJsonArray("parts")
                               .get(0).getAsJsonObject()
                               .get("text").getAsString();
                               
                case CUSTOM:
                    if (json.has("choices"))
                    {
                        return json.getAsJsonArray("choices")
                                   .get(0).getAsJsonObject()
                                   .getAsJsonObject("message")
                                   .get("content").getAsString();
                    }
                    return responseBody;
                    
                default:
                    return responseBody;
            }
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new RuntimeException("解析响应失败: " + e.getMessage(), e);
        }
    }
}
