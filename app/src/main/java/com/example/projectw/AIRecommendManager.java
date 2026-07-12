package com.example.projectw;

import android.content.Context;

import java.util.Random;

/**
 * AI 食物推荐管理器
 *
 * 调用链路：
 *   ShopActivity → AIRecommendManager.requestRecommendation()
 *     → 优先调用 AIService（真实 AI API）
 *     → 失败或未配置时降级为本地模拟推荐
 *
 * 接入真实 API 步骤：
 *   1. 在 App 中设置 API Key（ShopActivity 顶部输入框）
 *   2. AIRecommendManager 自动切换到 API 模式
 *   3. 无需修改其他任何代码
 */
public class AIRecommendManager {

    /** 推荐结果 */
    public static class RecommendResult {
        public String mainFood;
        public String sideFood;
        public String drink;
        public String recommendShop;

        public String toDisplayString() {
            return mainFood + " + " + sideFood + " + " + drink + "\n" + recommendShop;
        }
    }

    /** 回调接口 */
    public interface RecommendCallback {
        void onSuccess(RecommendResult result);
        void onError(String error);
    }

    /** -------- 本地模拟搭配库（API 不可用时的兜底） -------- */
    private static final RecommendResult[] LOCAL_DATA = {
            createResult("🍚 米饭", "🍅 番茄炒蛋", "🧃 冰红茶", "推荐店铺：蜀味川菜"),
            createResult("🍜 拉面", "🌭 烤肠", "🥤 柠檬水", "推荐店铺：老王面馆"),
            createResult("🍔 汉堡", "🍟 薯条", "🥤 可乐", "推荐店铺：麦香汉堡"),
            createResult("🍛 咖喱饭", "🥗 蔬菜沙拉", "🍵 绿茶", "推荐店铺：幸福茶餐厅"),
            createResult("🥟 饺子", "🍲 酸辣汤", "🧃 橙汁", "推荐店铺：老王面馆"),
            createResult("🍝 意面", "🍗 烤鸡翅", "🥤 雪碧", "推荐店铺：必胜披萨"),
            createResult("🍗 炸鸡", "🍚 泡菜炒饭", "🍺 大麦茶", "推荐店铺：韩式炸鸡屋"),
            createResult("🐟 剁椒鱼头", "🥬 手撕包菜", "🍚 米饭", "推荐店铺：湘味小厨"),
            createResult("🦆 烧鸭", "🥬 白灼菜心", "🍵 普洱", "推荐店铺：粤式烧腊"),
            createResult("🍜 牛肉面", "🫓 牛肉饼", "🍵 三炮台", "推荐店铺：兰州拉面馆"),
            createResult("🥟 蒸饺", "🍜 拌面", "🍲 炖罐", "推荐店铺：沙县小吃"),
            createResult("🍲 黄焖鸡", "🥔 土豆", "🍚 米饭", "推荐店铺：黄焖鸡米饭"),
            createResult("🍢 麻辣烫", "🥤 酸梅汤", "🫓 烧饼", "推荐店铺：麻辣烫工坊"),
            createResult("🥭 杨枝甘露", "🍮 双皮奶", "🍵 茉莉花茶", "推荐店铺：甜蜜时光甜品"),
            createResult("🍖 羊肉串", "🍆 烤茄子", "🍺 无酒精啤", "推荐店铺：大嘴烧烤"),
    };

    private static int lastIndex = -1;
    private static final Random RANDOM = new Random();

    private static RecommendResult createResult(String main, String side, String drink, String shop) {
        RecommendResult r = new RecommendResult();
        r.mainFood = main;
        r.sideFood = side;
        r.drink = drink;
        r.recommendShop = shop;
        return r;
    }

    /**
     * 请求 AI 食物推荐
     *
     * 优先级：
     *   1. 如果配置了 API Key → 调用真实 AI（基于数据库菜品数据）
     *   2. 未配置 API Key 或 API 失败 → 本地随机推荐
     *
     * @param ctx      Context（读取 API 配置 + 数据库）
     * @param dbHelper 数据库帮助类
     * @param callback 回调
     */
    public static void requestRecommendation(Context ctx, DBHelper dbHelper, RecommendCallback callback) {
        // 检查是否配置了 AI API
        if (AIService.isConfigured(ctx)) {
            // 调用真实 AI
            AIService.requestRecommendation(ctx, dbHelper, new AIService.AICallback() {
                @Override
                public void onSuccess(String content) {
                    // 解析 AI 返回的内容为 RecommendResult
                    RecommendResult result = parseAIResponse(content);
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        // 解析失败，降级为本地推荐
                        callback.onSuccess(getLocalFallback());
                    }
                }

                @Override
                public void onError(String error) {
                    // API 失败，降级为本地推荐
                    callback.onSuccess(getLocalFallback());
                }
            });
        } else {
            // 未配置 API Key，使用本地推荐
            callback.onSuccess(getLocalFallback());
        }
    }

    /**
     * 解析 AI 返回内容为 RecommendResult
     * AI 回复格式：主食：xxx | 配菜：xxx | 饮品：xxx | 推荐店铺：xxx
     */
    private static RecommendResult parseAIResponse(String content) {
        try {
            String mainFood = "", sideFood = "", drink = "", shop = "";

            // 按 "|" 分割
            String[] parts = content.split("\\|");
            for (String part : parts) {
                part = part.trim();
                if (part.contains("主食") || part.contains("主食：") || part.contains("主食:")) {
                    mainFood = part.replaceFirst(".*?[：:]", "").trim();
                } else if (part.contains("配菜") || part.contains("配菜：") || part.contains("配菜:")) {
                    sideFood = part.replaceFirst(".*?[：:]", "").trim();
                } else if (part.contains("饮品") || part.contains("饮品：") || part.contains("饮品:")) {
                    drink = part.replaceFirst(".*?[：:]", "").trim();
                } else if (part.contains("店铺") || part.contains("店铺：") || part.contains("店铺:")) {
                    shop = "推荐店铺：" + part.replaceFirst(".*?[：:]", "").trim();
                }
            }

            if (mainFood.isEmpty() && sideFood.isEmpty()) return null; // 解析失败

            // 没有饮品时给个默认
            if (drink.isEmpty()) drink = "🍵 推荐饮品";

            RecommendResult result = new RecommendResult();
            result.mainFood = mainFood.isEmpty() ? "🍚 推荐主食" : mainFood;
            result.sideFood = sideFood.isEmpty() ? "🥗 推荐配菜" : sideFood;
            result.drink = drink;
            result.recommendShop = shop.isEmpty() ? "推荐店铺：请见上方列表" : shop;
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /** 本地随机推荐（兜底方案） */
    private static RecommendResult getLocalFallback() {
        int index;
        do {
            index = RANDOM.nextInt(LOCAL_DATA.length);
        } while (LOCAL_DATA.length > 1 && index == lastIndex);
        lastIndex = index;
        return LOCAL_DATA[index];
    }

    /** 同步获取推荐（简单场景用） */
    public static RecommendResult getRecommendationSync() {
        return getLocalFallback();
    }
}
