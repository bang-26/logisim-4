package com.cburch.logisim.ai.ui;


import com.cburch.logisim.ai.config.AiConfig;
import com.cburch.logisim.ai.service.AiService;
import com.cburch.logisim.ai.service.AiServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AiConfigDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    
    private final AiConfig config;
    private final AiConfig originalConfig;
    private final AiService aiService;
    
    private JComboBox<String> modelComboBox;
    private JPasswordField apiKeyField;
    private JTextField apiUrlField;
    private boolean saved;
    private boolean isCustomModel;
    
    public AiConfigDialog(Frame parent, AiConfig config)
    {
        super(parent, "AI 模型配置", true);
        this.config = config;
        this.originalConfig = new AiConfig();
        this.originalConfig.setModelType(config.getModelType());
        this.originalConfig.setApiKey(config.getApiKey());
        this.originalConfig.setApiUrl(config.getApiUrl());
        this.aiService = new AiServiceImpl();
        this.saved = false;
        this.isCustomModel = (config.getModelType() == AiConfig.ModelType.CUSTOM);
        
        initializeUI();
    }
    
    private void initializeUI()
    {
        setLayout(new BorderLayout());
        setSize(550, 320);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel modelLabel = new JLabel("模型:");
        modelLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        mainPanel.add(modelLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        modelComboBox = new JComboBox<>(new String[]{
            "Qwen (通义千问)",
            "GPT (OpenAI)",
            "Gemini (Google)",
            "自定义模型"
        });
        modelComboBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        AiConfig.ModelType currentModel = config.getModelType();
        switch (currentModel)
        {
            case QWEN:
                modelComboBox.setSelectedIndex(0);
                break;
            case GPT:
                modelComboBox.setSelectedIndex(1);
                break;
            case GEMINI:
                modelComboBox.setSelectedIndex(2);
                break;
            case CUSTOM:
                modelComboBox.setSelectedIndex(3);
                break;
        }
        
        modelComboBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    updateApiUrlForSelectedModel();
                }
            }
        });
        
        mainPanel.add(modelComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel apiKeyLabel = new JLabel("API 密钥:");
        apiKeyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        mainPanel.add(apiKeyLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        apiKeyField = new JPasswordField(config.getApiKey());
        apiKeyField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        mainPanel.add(apiKeyField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel apiUrlLabel = new JLabel("API URL:");
        apiUrlLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        mainPanel.add(apiUrlLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String initialApiUrl = isCustomModel ? config.getApiUrl() : currentModel.getDefaultUrl();
        apiUrlField = new JTextField(initialApiUrl, 20);
        apiUrlField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        if (!isCustomModel)
        {
            apiUrlField.setEditable(false);
            apiUrlField.setBackground(new Color(240, 240, 240));
        }
        
        mainPanel.add(apiUrlField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 5, 5, 5);
        JLabel hintLabel = new JLabel("<html><font color='#888888' size='2'>选择模型后会自动填充对应的 API URL</font></html>");
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        mainPanel.add(hintLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton testButton = new JButton("测试连接");
        testButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        testButton.addActionListener(e -> testConnection());
        
        JButton saveButton = new JButton("保存");
        saveButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        saveButton.addActionListener(e ->
        {
            saveConfiguration();
            dispose();
        });
        
        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        cancelButton.addActionListener(e ->
        {
            config.setModelType(originalConfig.getModelType());
            config.setApiKey(originalConfig.getApiKey());
            config.setApiUrl(originalConfig.getApiUrl());
            dispose();
        });
        
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void updateApiUrlForSelectedModel()
    {
        AiConfig.ModelType selectedModel = getSelectedModelType();
        
        if (selectedModel == AiConfig.ModelType.CUSTOM)
        {
            apiUrlField.setEditable(true);
            apiUrlField.setBackground(Color.WHITE);
            apiUrlField.setText("");
            apiUrlField.requestFocusInWindow();
            isCustomModel = true;
        } else
        {
            apiUrlField.setEditable(false);
            apiUrlField.setBackground(new Color(240, 240, 240));
            apiUrlField.setText(selectedModel.getDefaultUrl());
            isCustomModel = false;
        }
    }
    
    private void testConnection()
    {
        AiConfig testConfig = new AiConfig();
        testConfig.setModelType(getSelectedModelType());
        testConfig.setApiKey(new String(apiKeyField.getPassword()));
        testConfig.setApiUrl(apiUrlField.getText().trim());
        
        if (testConfig.getApiKey().isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                "请输入 API 密钥",
                "警告",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (testConfig.getApiUrl().isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                "API URL 不能为空",
                "警告",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this,
            "正在测试连接...\n这可能需要几秒钟时间",
            "测试连接",
            JOptionPane.INFORMATION_MESSAGE);
        
        aiService.testConnection(testConfig)
            .thenAccept(success ->
            {
                SwingUtilities.invokeLater(() ->
                {
                    if (success)
                    {
                        JOptionPane.showMessageDialog(this,
                            "✅ 连接成功！\nAPI 配置正确，可以正常使用。",
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else
                    {
                        JOptionPane.showMessageDialog(this,
                            "❌ 连接失败\n请检查 API 密钥和网络连接",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            })
            .exceptionally(error ->
            {
                SwingUtilities.invokeLater(() ->
                {
                    String errorMsg = error.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty())
                    {
                        errorMsg = error.getClass().getSimpleName();
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "❌ 连接错误\n\n" + errorMsg + "\n\n请查看控制台获取详细信息",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
    }
    
    private void saveConfiguration()
    {
        AiConfig.ModelType selectedModel = getSelectedModelType();
        String apiUrl = apiUrlField.getText().trim();
        
        if (apiUrl.isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                "API URL 不能为空",
                "警告",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        config.setModelType(selectedModel);
        config.setApiKey(new String(apiKeyField.getPassword()));
        config.setApiUrl(apiUrl);
        config.saveToPreferences();
        saved = true;
        
        System.out.println("✅ AI 配置已保存");
        System.out.println("   模型: " + selectedModel);
        System.out.println("   API URL: " + apiUrl);
    }
    
    private AiConfig.ModelType getSelectedModelType()
    {
        int selectedIndex = modelComboBox.getSelectedIndex();
        switch (selectedIndex)
        {
            case 0:
                return AiConfig.ModelType.QWEN;
            case 1:
                return AiConfig.ModelType.GPT;
            case 2:
                return AiConfig.ModelType.GEMINI;
            case 3:
                return AiConfig.ModelType.CUSTOM;
            default:
                return AiConfig.ModelType.GPT;
        }
    }
    
    public boolean isSaved()
    {
        return saved;
    }
}