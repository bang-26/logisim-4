package com.cburch.logisim.ai.service;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.ai.service
 * ClassName     ：  AiService
 * CreateTime    ：  2026-04-19 16:05
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */


import com.cburch.logisim.ai.config.AiConfig;
import com.cburch.logisim.ai.model.ChatMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiService
{
    CompletableFuture<String> sendMessage(List<ChatMessage> messages, AiConfig config);
    
    CompletableFuture<Boolean> testConnection(AiConfig config);
}