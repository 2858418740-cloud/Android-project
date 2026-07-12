package com.example.projectw;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * AI 服务类 — 调用云端 AI API 获取食物推荐
 * 使用 Android 内置 HttpURLConnection + org.json，无需任何第三方依赖
 *
 * 支持的 API（OpenAI 兼容格式）：
 *   - DeepSeek:   https://api.deepseek.com/v1/chat/completions
 *   - OpenAI:     https://api.openai.com/v1/chat/completions
 *   - Groq:       https://api.groq.com/openai/v1/chat/completions
 *   - 硅基流动:    https://api.siliconflow.cn/v1/chat/completions
 *   - 通义千问:    https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
 */
public class AIService {

    private static final String PREFS_NAME = "ai_config";

    // ==================== API 配置 ====================
    // 平台：硅基流动 SiliconFlow（免费额度）
    // 模型：Qwen2.5-7B-Instruct（中文强、速度快）
    private static final String DEFAULT_API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String DEFAULT_MODEL = "Qwen/Qwen2.5-7B-Instruct";
    private static final String HARDCODED_API_KEY = "sk-vjdyagvqejrbyvdiijmgzaweyexyjvgxdzlyqxaibrfhsyno";

    /** 回调接口 */
    public interface AICallback {
        void onSuccess(String content);
        void onError(String error);
    }

    /**
     * 保存 API 配置到 SharedPreferences
     */
    public static void saveConfig(Context ctx, String apiKey, String apiUrl, String model) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString("api_key", apiKey)
                .putString("api_url", apiUrl.isEmpty() ? DEFAULT_API_URL : apiUrl)
                .putString("model", model.isEmpty() ? DEFAULT_MODEL : model)
                .apply();
    }

    /** 获取 API Key（优先读取用户配置，否则使用内置 Key） */
    public static String getApiKey(Context ctx) {
        String userKey = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("api_key", "");
        return userKey.isEmpty() ? HARDCODED_API_KEY : userKey;
    }

    /** 是否已配置 API（内置了默认 Key，始终返回 true） */
    public static boolean isConfigured(Context ctx) {
        return true;
    }

    /** 获取 API URL */
    private static String getApiUrl(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("api_url", DEFAULT_API_URL);
    }

    /** 获取模型名 */
    private static String getModel(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString("model", DEFAULT_MODEL);
    }

    /**
     * 请求 AI 食物推荐
     *
     * @param ctx      Context
     * @param dbHelper 数据库帮助类（用于读取菜品数据作为上下文）
     * @param callback 回调
     */
    public static void requestRecommendation(Context ctx, DBHelper dbHelper, AICallback callback) {
        new Thread(() -> {
            try {
                String apiKey = getApiKey(ctx);
                if (apiKey.isEmpty()) {
                    callback.onError("请先设置 API Key");
                    return;
                }

                // 构建请求体
                String systemPrompt = buildSystemPrompt(dbHelper);
                String requestBody = new JSONObject()
                        .put("model", getModel(ctx))
                        .put("messages", new JSONArray()
                                .put(new JSONObject()
                                        .put("role", "system")
                                        .put("content", systemPrompt))
                                .put(new JSONObject()
                                        .put("role", "user")
                                        .put("content", "请根据以上店铺和菜品数据，为我推荐一份今日最佳搭配（主食+配菜+饮品），并说明推荐理由和对应的店铺名称。回复格式：主食：xxx | 配菜：xxx | 饮品：xxx | 推荐店铺：xxx"))
                        )
                        .put("temperature", 0.8)
                        .put("max_tokens", 300)
                        .toString();

                // HTTP 请求
                URL url = new URL(getApiUrl(ctx));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();

                    // 解析响应
                    JSONObject json = new JSONObject(response.toString());
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    callback.onSuccess(content.trim());
                } else {
                    // 读取错误信息
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) err.append(line);
                    br.close();
                    callback.onError("API 请求失败 (HTTP " + code + "): " + err);
                }
                conn.disconnect();
            } catch (Exception e) {
                callback.onError("网络错误: " + e.getMessage());
            }
        }).start();
    }

    /**
     * AI 对话（用于聊天弹窗）
     * @param ctx      Context
     * @param prompt   用户问题（已包含系统上下文）
     * @param callback 回调
     */
    public static void chat(Context ctx, String prompt, AICallback callback) {
        new Thread(() -> {
            try {
                String apiKey = getApiKey(ctx);
                String requestBody = new JSONObject()
                        .put("model", getModel(ctx))
                        .put("messages", new JSONArray()
                                .put(new JSONObject()
                                        .put("role", "system")
                                        .put("content", "你是一个热情的美食推荐助手。"))
                                .put(new JSONObject()
                                        .put("role", "user")
                                        .put("content", prompt)))
                        .put("temperature", 0.8)
                        .put("max_tokens", 400)
                        .toString();

                URL url = new URL(getApiUrl(ctx));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();
                    JSONObject json = new JSONObject(response.toString());
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    callback.onSuccess(content.trim());
                } else {
                    callback.onError("HTTP " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * 构建系统提示词：将所有店铺和菜品数据作为上下文
     */
    private static String buildSystemPrompt(DBHelper dbHelper) {
        List<Shop> shops = dbHelper.getAllShops();
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个美食推荐助手。以下是当前可用的店铺和菜品数据：\n\n");

        for (Shop shop : shops) {
            sb.append("【").append(shop.getShopName()).append("】")
                    .append(" 评分⭐").append(String.format("%.1f", shop.getRating()))
                    .append(" 月售").append(shop.getSaleCount())
                    .append(" 类型").append(shop.getDineType())
                    .append("\n");
            sb.append("  简介：").append(shop.getShopDesc()).append("\n");
            sb.append("  菜品：");

            List<Food> foods = dbHelper.getFoodsByShopId(shop.getId());
            for (int i = 0; i < foods.size(); i++) {
                Food f = foods.get(i);
                if (i > 0) sb.append("、");
                sb.append(f.getFoodName()).append("(¥").append((int) f.getPrice()).append(")");
            }
            sb.append("\n\n");
        }

        sb.append("请根据以上真实数据推荐一份合理的今日搭配（主食+配菜+饮品），优先推荐评分高、月销量高的店铺。");
        sb.append("必须从上述菜品中选择，回复格式固定为：");
        sb.append("主食：xxx | 配菜：xxx | 饮品：xxx | 推荐店铺：xxx");
        return sb.toString();
    }
}
