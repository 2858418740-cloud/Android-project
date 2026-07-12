package com.example.projectw;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 店铺首页
 * AI 推荐卡片 + 对话 + 一键采纳 + 店铺列表
 */
public class ShopActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnClearSearch;
    private RecyclerView rvShopList;
    private LinearLayout layoutCartBar;
    private TextView tvCartCount;
    private TextView tvCartTotal;
    private CartPanelManager cartPanel;

    private DBHelper dbHelper;
    private String username;
    private ShopAdapter shopAdapter;
    private List<Shop> allShopList;
    private String currentAiResult = ""; // 保存当前 AI 推荐原文
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        username = getIntent().getStringExtra("username");
        dbHelper = new DBHelper(this);

        initView();
        loadShopList();
        loadAiRecommendation();
        setupSearch();
    }

    private void initView() {
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        rvShopList = findViewById(R.id.rv_shop_list);
        layoutCartBar = findViewById(R.id.layout_cart_bar);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartTotal = findViewById(R.id.tv_cart_total);
        rvShopList.setLayoutManager(new LinearLayoutManager(this));

        // 我的订单入口
        findViewById(R.id.btn_my_orders).setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, OrderHistoryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterShops(s.toString().trim());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });
    }

    private void filterShops(String keyword) {
        if (keyword.isEmpty()) {
            shopAdapter.updateList(allShopList);
        } else {
            shopAdapter.updateList(dbHelper.searchShops(keyword));
        }
    }

    // ==================== AI 推荐 ====================

    private void loadAiRecommendation() {
        AIRecommendManager.requestRecommendation(this, dbHelper,
                new AIRecommendManager.RecommendCallback() {
                    @Override
                    public void onSuccess(AIRecommendManager.RecommendResult result) {
                        currentAiResult = result.toDisplayString();
                        mainHandler.post(() -> shopAdapter.updateAiText(currentAiResult));
                    }
                    @Override
                    public void onError(String error) {
                        mainHandler.post(() -> {
                            Toast.makeText(ShopActivity.this, error, Toast.LENGTH_SHORT).show();
                            AIRecommendManager.RecommendResult fallback =
                                    AIRecommendManager.getRecommendationSync();
                            if (fallback != null) {
                                currentAiResult = fallback.toDisplayString();
                                shopAdapter.updateAiText(currentAiResult);
                            }
                        });
                    }
                });
    }

    // ==================== 一键采纳 ====================

    private void onAdoptRecommendation() {
        if (currentAiResult.isEmpty()) {
            Toast.makeText(this, "AI 推荐还在加载中", Toast.LENGTH_SHORT).show();
            return;
        }

        // 从 AI 推荐文字中提取食物名，匹配数据库
        List<Food> matchedFoods = matchFoodsFromAiText(currentAiResult);

        if (matchedFoods.isEmpty()) {
            Toast.makeText(this, "未能匹配到具体菜品，请先与 AI 对话确认", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建确认弹窗
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Food f : matchedFoods) {
            sb.append("• ").append(f.getFoodName())
                    .append("  ¥").append(formatPrice(f.getPrice())).append("\n");
            total += f.getPrice();
        }
        sb.append("\n合计：¥").append(formatPrice(total));

        new AlertDialog.Builder(this)
                .setTitle("🛒 确认加入购物车")
                .setMessage("AI 推荐的以下菜品将加入购物车：\n\n" + sb.toString())
                .setPositiveButton("加入购物车", (dialog, which) -> {
                    for (Food f : matchedFoods) {
                        dbHelper.addToCart(username, f.getId(), f.getFoodName(), f.getPrice());
                    }
                    animateCartBar(); // 带数字跳动动画更新购物车栏
                    Toast.makeText(this, "已加入 " + matchedFoods.size() + " 件商品到购物车", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 从 AI 返回文字中匹配数据库菜品 */
    private List<Food> matchFoodsFromAiText(String aiText) {
        List<Food> result = new ArrayList<>();
        List<Food> allFoods = new ArrayList<>();
        for (Shop s : allShopList) {
            allFoods.addAll(dbHelper.getFoodsByShopId(s.getId()));
        }

        // 在 AI 返回文字中搜索每个菜品名
        for (Food f : allFoods) {
            String name = f.getFoodName();
            // 去掉括号内容做模糊匹配
            String shortName = name.replaceAll("\\(.*?\\)", "").replaceAll("（.*?）", "").trim();
            if (aiText.contains(shortName) || aiText.contains(name)) {
                if (!result.contains(f)) result.add(f);
            }
        }

        // 限制最多 5 件
        if (result.size() > 5) result = result.subList(0, 5);
        return result;
    }

    // ==================== AI 对话 ====================

    private String lastAiChatResponse = ""; // 记录对话中最后一次 AI 回复

    private void showChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💬 AI 美食助手");

        LinearLayout chatLayout = new LinearLayout(this);
        chatLayout.setOrientation(LinearLayout.VERTICAL);
        chatLayout.setPadding(30, 16, 30, 8);

        // 聊天记录区
        ScrollView scrollView = new ScrollView(this);
        TextView tvChatHistory = new TextView(this);
        tvChatHistory.setText("🤖 你好！我是 AI 美食助手。\n你可以问我：有什么好吃的推荐？有什么优惠？哪个店评分最高？\n\n");
        tvChatHistory.setTextSize(14);
        tvChatHistory.setTextColor(0xFF333333);
        tvChatHistory.setLineSpacing(4, 1);
        tvChatHistory.setPadding(0, 0, 0, 12);
        scrollView.addView(tvChatHistory);
        chatLayout.addView(scrollView);

        // 输入区
        LinearLayout inputLayout = new LinearLayout(this);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);

        EditText etInput = new EditText(this);
        etInput.setHint("输入你的问题...");
        etInput.setTextSize(14);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        inputLayout.addView(etInput, inputParams);

        Button btnSend = new Button(this);
        btnSend.setText("发送");
        btnSend.setTextSize(13);
        btnSend.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFD700));
        btnSend.setTextColor(0xFF000000);
        inputLayout.addView(btnSend);

        chatLayout.addView(inputLayout);

        // 一键采纳按钮
        Button btnAdopt = new Button(this);
        btnAdopt.setText("🛒 一键采纳最后一轮推荐，加入购物车");
        btnAdopt.setTextSize(13);
        btnAdopt.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFD700));
        btnAdopt.setTextColor(0xFF000000);
        LinearLayout.LayoutParams adoptParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        adoptParams.topMargin = 10;
        chatLayout.addView(btnAdopt, adoptParams);

        builder.setView(chatLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // 发送按钮
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (question.isEmpty()) return;

            tvChatHistory.append("👤 " + question + "\n\n");
            etInput.setText("");
            btnSend.setEnabled(false);
            btnSend.setText("思考中...");

            String chatPrompt = buildChatPrompt(question);
            AIService.chat(this, chatPrompt, new AIService.AICallback() {
                @Override
                public void onSuccess(String content) {
                    String reply = content.trim();
                    lastAiChatResponse = reply;
                    mainHandler.post(() -> {
                        tvChatHistory.append("🤖 " + reply + "\n\n");
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                        btnSend.setEnabled(true);
                        btnSend.setText("发送");
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        tvChatHistory.append("🤖 （抱歉，网络出问题了：" + error + "）\n\n");
                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                        btnSend.setEnabled(true);
                        btnSend.setText("发送");
                    });
                }
            });
        });

        // 一键采纳：解析最后一次 AI 回复中的菜品
        btnAdopt.setOnClickListener(v -> {
            if (lastAiChatResponse.isEmpty()) {
                Toast.makeText(this, "请先让 AI 推荐菜品", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Food> matched = matchFoodsFromAiText(lastAiChatResponse);
            if (matched.isEmpty()) {
                Toast.makeText(this, "未能匹配到菜品，请让 AI 明确推荐菜名", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder sb = new StringBuilder();
            double total = 0;
            for (Food f : matched) {
                sb.append("• ").append(f.getFoodName()).append("  ¥").append(formatPrice(f.getPrice())).append("\n");
                total += f.getPrice();
            }
            sb.append("\n合计：¥").append(formatPrice(total));

            new AlertDialog.Builder(this)
                    .setTitle("🛒 确认加入购物车")
                    .setMessage("AI 推荐的以下菜品将加入购物车：\n\n" + sb.toString())
                    .setPositiveButton("加入购物车", (dlg, which) -> {
                        for (Food f : matched) {
                            dbHelper.addToCart(username, f.getId(), f.getFoodName(), f.getPrice());
                        }
                        animateCartBar(); // 更新购物车栏
                        Toast.makeText(this, "已加入 " + matched.size() + " 件商品到购物车", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    /** 构建对话上下文 */
    private String buildChatPrompt(String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个美食推荐助手。当前可用的店铺和菜品如下：\n");
        for (Shop s : allShopList) {
            sb.append(s.getShopName()).append("（⭐").append(String.format("%.1f", s.getRating()))
                    .append(" 月售").append(s.getSaleCount()).append("）：");
            List<Food> foods = dbHelper.getFoodsByShopId(s.getId());
            for (int i = 0; i < foods.size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(foods.get(i).getFoodName()).append("¥").append((int) foods.get(i).getPrice());
            }
            sb.append("\n");
        }
        sb.append("\n用户问题：").append(question);
        sb.append("\n请简短回答（50字以内），基于以上真实数据推荐。");
        return sb.toString();
    }

    // ==================== 店铺列表 ====================

    private void loadShopList() {
        allShopList = dbHelper.getAllShops();
        shopAdapter = new ShopAdapter(allShopList,
                shop -> {
                    Intent intent = new Intent(ShopActivity.this, ShopDetailActivity.class);
                    intent.putExtra("shop_id", shop.getId());
                    intent.putExtra("shop_name", shop.getShopName());
                    intent.putExtra("username", username);
                    startActivity(intent);
                },
                new ShopAdapter.OnAiActionListener() {
                    @Override public void onRefresh() { loadAiRecommendation(); }
                    @Override public void onChat() { showChatDialog(); }
                    @Override public void onAdopt() { onAdoptRecommendation(); }
                });
        rvShopList.setAdapter(shopAdapter);

        // 购物车面板管理器
        cartPanel = new CartPanelManager(this, dbHelper, username, () -> {
            refreshCartBar();
            if (cartPanel != null) cartPanel.refresh();
        });

        // 左侧信息区 + 箭头 → 展开/收起面板
        findViewById(R.id.layout_cart_info).setOnClickListener(v -> cartPanel.toggle());
        findViewById(R.id.btn_toggle_panel).setOnClickListener(v -> cartPanel.toggle());
        // 结算按钮 → 直接进入订单页
        findViewById(R.id.btn_settle).setOnClickListener(v -> cartPanel.goToOrder());
    }

    private void refreshCartBar() {
        int count = dbHelper.getCartTotalCount(username);
        double total = dbHelper.getCartTotalPrice(username);
        tvCartCount.setText(count + " 份");
        tvCartTotal.setText(formatPrice(total));
    }

    /** 带动画的购物车刷新（数字从旧值跳到新值） */
    private void animateCartBar() {
        int toCount = dbHelper.getCartTotalCount(username);
        double toTotal = dbHelper.getCartTotalPrice(username);

        // 解析旧数量
        String oldText = tvCartCount.getText().toString().replace(" 份", "");
        int fromCount = 0;
        try { fromCount = Integer.parseInt(oldText); } catch (Exception ignored) {}

        // 数量动画
        ValueAnimator countAnim = ValueAnimator.ofInt(fromCount, toCount);
        countAnim.setDuration(400);
        countAnim.addUpdateListener(anim ->
                tvCartCount.setText(anim.getAnimatedValue() + " 份"));
        countAnim.start();

        // 价格动画
        double oldTotal = 0;
        try { oldTotal = Double.parseDouble(tvCartTotal.getText().toString()); } catch (Exception ignored) {}
        ValueAnimator totalAnim = ValueAnimator.ofFloat((float) oldTotal, (float) toTotal);
        totalAnim.setDuration(400);
        totalAnim.addUpdateListener(anim -> {
            float val = (float) anim.getAnimatedValue();
            tvCartTotal.setText(formatPrice(val));
        });
        totalAnim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBar();
    }

    private String formatPrice(double p) {
        if (p == (int) p) return String.valueOf((int) p);
        return String.format(Locale.US, "%.2f", p);
    }
}
