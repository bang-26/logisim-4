package com.cburch.logisim.gui.web;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.gui.web
 * ClassName     ：  WebBrowserPanel
 * CreateTime    ：  2026-04-27 20:33
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class WebBrowserPanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    private JFXPanel jfxPanel;
    private WebView webView;
    private JFrame frame;
    private CookieManager cookieManager;
    
    public WebBrowserPanel(String url, String title)
    {
        setLayout(new BorderLayout());
        
        // Initialize cookie manager for session persistence
        initializeCookieManager();
        
        // Create the JFXPanel and add it to this panel
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
        
        // Initialize JavaFX content on the JavaFX Application Thread
        Platform.runLater(() -> initFX(url));
        
        // Create frame to hold the browser
        frame = new JFrame(title);
        frame.getContentPane().add(this);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        
        // Handle window closing
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                cleanup();
            }
        });
    }
    
    private void initializeCookieManager()
    {
        try
        {
            cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);
            System.out.println("Cookie管理器已初始化");
        } catch (Exception e)
        {
            System.err.println("Cookie管理器初始化失败: " + e.getMessage());
        }
    }
    
    private void initFX(String url)
    {
        // Create a WebView instance
        webView = new WebView();
        WebEngine engine = webView.getEngine();
        
        // Set user agent to mimic a real browser (important for QR login)
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        // Enable JavaScript (critical for QR code login)
        engine.setJavaScriptEnabled(true);
        
        // Enable local storage for session persistence
        try
        {
            engine.executeScript("if (typeof localStorage === 'undefined') { window.localStorage = { getItem: function() {}, setItem: function() {}, removeItem: function() {} }; }");
        } catch (Exception e)
        {
            System.err.println("LocalStorage初始化警告: " + e.getMessage());
        }
        
        // Add progress listener to handle page load events
        engine.getLoadWorker().stateProperty().addListener(
            (ov, oldState, newState) ->
            {
                if (newState == Worker.State.SUCCEEDED)
                {
                    String currentUrl = engine.getLocation();
                    System.out.println("✅ 页面加载完成: " + currentUrl);
                    
                    // Check if login was successful by monitoring URL changes
                    if (isLoginSuccessful(currentUrl))
                    {
                        System.out.println("🎉 检测到登录成功！");
                    }
                    
                    // Execute JavaScript to enable better interaction
                    try
                    {
                        JSObject window = (JSObject) engine.executeScript("window");
                        if (window != null)
                        {
                            System.out.println("✓ JavaScript环境初始化成功");
                            
                            // Inject script to monitor login status
                            injectLoginMonitor(engine);
                        }
                    } catch (Exception e)
                    {
                        System.err.println("JavaScript执行错误: " + e.getMessage());
                    }
                } else if (newState == Worker.State.FAILED)
                {
                    System.err.println("❌ 页面加载失败");
                } else if (newState == Worker.State.RUNNING)
                {
                    System.out.println("⏳ 页面加载中...");
                }
            }
        );
        
        // Handle location changes (redirects) - critical for login flow
        engine.locationProperty().addListener((obs, oldLocation, newLocation) ->
        {
            System.out.println("🔄 页面跳转: " + oldLocation + " -> " + newLocation);
            
            // Monitor for login success redirects
            if (oldLocation != null && newLocation != null)
            {
                if (isLoginRedirect(oldLocation, newLocation))
                {
                    System.out.println("✨ 检测到登录重定向！");
                }
            }
        });
        
        // Enable history navigation
        WebHistory history = engine.getHistory();
        history.getEntries().addListener((javafx.collections.ListChangeListener.Change<? extends WebHistory.Entry> c) ->
        {
            while (c.next())
            {
                if (c.wasAdded())
                {
                    for (WebHistory.Entry entry : c.getAddedSubList())
                    {
                        System.out.println("📜 历史记录: " + entry.getUrl());
                    }
                }
            }
        });
        
        // Load the specified URL
        System.out.println("🌐 开始加载URL: " + url);
        engine.load(url);
        
        // Create a scene with the WebView
        Scene scene = new Scene(webView);
        jfxPanel.setScene(scene);
    }
    
    private boolean isLoginSuccessful(String url)
    {
        if (url == null) return false;
        
        // Check common login success indicators
        return url.contains("/home") ||
                   url.contains("/dashboard") ||
                   url.contains("/main") ||
                   (url.contains("tongyi.aliyun.com") && !url.contains("login")) ||
                   url.contains("user/profile");
    }
    
    private boolean isLoginRedirect(String oldUrl, String newUrl)
    {
        if (oldUrl == null || newUrl == null) return false;
        
        // Detect redirect from login page to main page
        boolean wasOnLoginPage = oldUrl.contains("login") || oldUrl.contains("auth");
        boolean isNowOnMainPage = !newUrl.contains("login") && !newUrl.contains("auth");
        
        return wasOnLoginPage && isNowOnMainPage;
    }
    
    private void injectLoginMonitor(WebEngine engine)
    {
        try
        {
            // Inject JavaScript to monitor login status changes
            String script =
                "(function() {" +
                    "  console.log('Login monitor injected');" +
                    "  " +
                    "  // Monitor for login success events" +
                    "  var checkLoginStatus = function() {" +
                    "    var cookies = document.cookie;" +
                    "    if (cookies && cookies.length > 0) {" +
                    "      console.log('Cookies detected: ' + cookies.substring(0, 50));" +
                    "    }" +
                    "  };" +
                    "  " +
                    "  // Check every 2 seconds" +
                    "  setInterval(checkLoginStatus, 2000);" +
                    "  " +
                    "  // Listen for storage changes (for cross-tab login)" +
                    "  window.addEventListener('storage', function(e) {" +
                    "    console.log('Storage changed: ' + e.key);" +
                    "  });" +
                    "})();" +
                    "";
            
            engine.executeScript(script);
            System.out.println("✓ 登录监控脚本已注入");
        } catch (Exception e)
        {
            System.err.println("注入监控脚本失败: " + e.getMessage());
        }
    }
    
    public void show()
    {
        frame.setVisible(true);
    }
    
    public void dispose()
    {
        if (frame != null)
        {
            frame.dispose();
        }
        cleanup();
    }
    
    private void cleanup()
    {
        if (webView != null)
        {
            Platform.runLater(() ->
            {
                try
                {
                    webView.getEngine().load(null);
                    webView = null;
                    System.out.println("浏览器资源已清理");
                } catch (Exception e)
                {
                    System.err.println("清理资源时出错: " + e.getMessage());
                }
            });
        }
    }
}
