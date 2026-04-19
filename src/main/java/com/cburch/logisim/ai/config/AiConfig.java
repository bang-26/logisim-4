package com.cburch.logisim.ai.config;


import java.util.prefs.Preferences;

public class AiConfig
{
    private static final String PREF_NODE = "com.cburch.logisim.ai";
    private static final String KEY_MODEL = "model";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_API_URL = "api_url";
    
    public enum ModelType
    {
        QWEN("qwen", "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"),
        GPT("gpt", "https://api.openai.com/v1/chat/completions"),
        GEMINI("gemini", "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"),
        CUSTOM("custom", "");
        
        private final String id;
        private final String defaultUrl;
        
        ModelType(String id, String defaultUrl)
        {
            this.id = id;
            this.defaultUrl = defaultUrl;
        }
        
        public String getId()
        {
            return id;
        }
        
        public String getDefaultUrl()
        {
            return defaultUrl;
        }
        
        public static ModelType fromId(String id)
        {
            for (ModelType type : values())
            {
                if (type.id.equals(id))
                {
                    return type;
                }
            }
            return GPT;
        }
    }
    
    private ModelType modelType;
    private String apiKey;
    private String apiUrl;
    
    public AiConfig()
    {
        this.modelType = ModelType.QWEN;
        this.apiKey = "";
        this.apiUrl = "";
    }
    
    public void loadFromPreferences()
    {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        this.modelType = ModelType.fromId(prefs.get(KEY_MODEL, ModelType.QWEN.getId()));
        this.apiKey = prefs.get(KEY_API_KEY, "");
        this.apiUrl = prefs.get(KEY_API_URL, "");
        
        System.out.println("📋 AI 配置已加载:");
        System.out.println("   模型: " + modelType);
        System.out.println("   API Key: " + (apiKey.isEmpty() ? "(未设置)" : "***" + apiKey.substring(Math.max(0, apiKey.length() - 4))));
        System.out.println("   API URL: " + getApiUrl());
    }
    
    public void saveToPreferences()
    {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        prefs.put(KEY_MODEL, modelType.getId());
        prefs.put(KEY_API_KEY, apiKey);
        prefs.put(KEY_API_URL, apiUrl != null ? apiUrl : "");
        
        System.out.println("💾 AI 配置已保存到系统偏好");
    }
    
    public ModelType getModelType()
    {
        return modelType;
    }
    
    public void setModelType(ModelType modelType)
    {
        this.modelType = modelType;
    }
    
    public String getApiKey()
    {
        return apiKey;
    }
    
    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }
    
    public String getApiUrl()
    {
        if (apiUrl != null && !apiUrl.trim().isEmpty())
        {
            return apiUrl.trim();
        }
        return modelType.getDefaultUrl();
    }
    
    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }
}