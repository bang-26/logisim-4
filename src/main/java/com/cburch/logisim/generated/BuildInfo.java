package com.cburch.logisim.generated;

/**
 * **************************************************************************************************
 * ProjectName   ：  logisim-4
 * Package       ：  com.cburch.logisim.generated
 * ClassName     ：  BuildInfo
 * CreateTime    ：  2026-04-19 14:23
 * Author        ：  Issac_Al
 * Email         ：  IssacAl@qq.com
 * IDE           ：  IntelliJ IDEA 2020.3.4
 * Version       ：  1.0
 * CodedFormat   ：  utf-8
 * Description   ：  Java Class
 * **************************************************************************************************
 */

import com.cburch.logisim.LogisimVersion;

import java.util.Date;

public final class BuildInfo
{
    // Build time VCS details
    public static final String branchName = "main";
    public static final String branchLastCommitHash = "a61ba571";
    public static final String buildId = "main/a61ba571";
    
    // Project build timestamp
    public static final long millis = 1776579734241L; // keep trailing 'L'
    public static final String year = "2026";
    public static final String dateIso8601 = "2026-04-19T14:22:14+0800";
    public static final Date date = new Date();
    
    static {date.setTime(millis);}
    
    // Project version
    public static final LogisimVersion version = LogisimVersion.fromString("4.2.0dev");
    public static final String name = "Logisim-evolution";
    public static final String displayName = "Logisim-evolution v4.2.0dev";
    public static final String url = "https://github.com/logisim-evolution/";
    
    // JRE info
    public static final String jvm_version =
        String.format("%s v%s", System.getProperty("java.vm.name"), System.getProperty("java.version"));
    public static final String jvm_vendor = System.getProperty("java.vendor");
}

