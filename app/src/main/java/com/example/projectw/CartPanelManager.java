package com.example.projectw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * 可展开购物车面板管理器
 * 箭头 ▲/▼ 展开/收起面板 | 结算按钮直达订单页
 */
public class CartPanelManager {

    private final Activity activity;
    private final DBHelper dbHelper;
    private final String username;

    private final View panelCart;
    private final LinearLayout containerCartItems;
    private final TextView tvPanelDeliveryFee, tvPanelPackagingFee;
    private final Button btnClearCart;
    private final TextView btnToggleArrow; // 箭头按钮

    private final Runnable onDataChanged;

    private static final double DELIVERY_FEE = 3.0;
    private static final double PACKING_FEE = 1.0;

    public CartPanelManager(Activity activity, DBHelper dbHelper, String username,
                            Runnable onDataChanged) {
        this.activity = activity;
        this.dbHelper = dbHelper;
        this.username = username;
        this.onDataChanged = onDataChanged;

        panelCart = activity.findViewById(R.id.panel_cart);
        containerCartItems = activity.findViewById(R.id.container_cart_items);
        tvPanelDeliveryFee = activity.findViewById(R.id.tv_panel_delivery_fee);
        tvPanelPackagingFee = activity.findViewById(R.id.tv_panel_packaging_fee);
        btnClearCart = activity.findViewById(R.id.btn_clear_cart);
        btnToggleArrow = activity.findViewById(R.id.btn_toggle_panel);

        // 清空按钮
        btnClearCart.setOnClickListener(v -> {
            if (dbHelper.getCartTotalCount(username) == 0) return;
            new AlertDialog.Builder(activity)
                    .setTitle("清空购物车")
                    .setMessage("确定要清空所有菜品吗？")
                    .setPositiveButton("清空", (d, w) -> {
                        dbHelper.clearCartByUser(username);
                        refresh();
                        if (onDataChanged != null) onDataChanged.run();
                        Toast.makeText(activity, "已清空", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    /** 展开/收起面板，并更新箭头方向 */
    public void toggle() {
        if (dbHelper.getCartTotalCount(username) == 0) {
            Toast.makeText(activity, "购物车为空，请先添加菜品", Toast.LENGTH_SHORT).show();
            return;
        }
        if (panelCart.getVisibility() == View.VISIBLE) {
            collapse();
        } else {
            refresh();
            panelCart.setVisibility(View.VISIBLE);
            btnToggleArrow.setText("▼");
        }
    }

    /** 收起面板 */
    public void collapse() {
        panelCart.setVisibility(View.GONE);
        btnToggleArrow.setText("▲");
    }

    /** 结算按钮 → 直接进订单页 */
    public void goToOrder() {
        if (dbHelper.getCartTotalCount(username) == 0) {
            Toast.makeText(activity, "购物车为空，请先添加菜品", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(activity, OrderActivity.class);
        intent.putExtra("username", username);
        activity.startActivity(intent);
    }

    /** 刷新面板内容 */
    public void refresh() {
        List<CartItem> items = dbHelper.getCartByUser(username);
        containerCartItems.removeAllViews();

        for (CartItem item : items) {
            containerCartItems.addView(createItemRow(item));
        }

        int totalCount = dbHelper.getCartTotalCount(username);
        tvPanelDeliveryFee.setText("¥" + formatPrice(DELIVERY_FEE));
        tvPanelPackagingFee.setText("¥" + formatPrice(PACKING_FEE * totalCount));
    }

    private LinearLayout createItemRow(CartItem item) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        TextView tvName = new TextView(activity);
        tvName.setText(item.getFoodName());
        tvName.setTextSize(13);
        tvName.setTextColor(0xFF2D2D2D);
        row.addView(tvName, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView tvPrice = new TextView(activity);
        tvPrice.setText("¥" + formatPrice(item.getPrice()));
        tvPrice.setTextSize(12);
        tvPrice.setTextColor(0xFF6B6B6B);
        tvPrice.setPadding(0, 0, 12, 0);
        row.addView(tvPrice);

        Button btnMinus = makeSmallBtn("−");
        btnMinus.setOnClickListener(v -> {
            if (item.getCount() <= 1) {
                new AlertDialog.Builder(activity)
                        .setMessage("确定删除「" + item.getFoodName() + "」吗？")
                        .setPositiveButton("删除", (d, w) -> {
                            dbHelper.removeCartItem(username, item.getFoodId());
                            refresh();
                            if (onDataChanged != null) onDataChanged.run();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                dbHelper.removeCartItem(username, item.getFoodId());
                dbHelper.addToCartWithCount(username, item.getFoodId(),
                        item.getFoodName(), item.getPrice(), item.getCount() - 1);
                refresh();
                if (onDataChanged != null) onDataChanged.run();
            }
        });
        row.addView(btnMinus);

        TextView tvCount = new TextView(activity);
        tvCount.setText(String.valueOf(item.getCount()));
        tvCount.setTextSize(14);
        tvCount.setTextColor(0xFF2D2D2D);
        tvCount.setGravity(Gravity.CENTER);
        tvCount.setMinWidth(28);
        tvCount.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        row.addView(tvCount);

        Button btnPlus = makeSmallBtn("+");
        btnPlus.setOnClickListener(v -> {
            dbHelper.addToCart(username, item.getFoodId(), item.getFoodName(), item.getPrice());
            refresh();
            if (onDataChanged != null) onDataChanged.run();
        });
        row.addView(btnPlus);

        Button btnDel = makeSmallBtn("✕");
        btnDel.setTextColor(0xFFFF4757);
        btnDel.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setMessage("确定删除「" + item.getFoodName() + "」吗？")
                    .setPositiveButton("删除", (d, w) -> {
                        dbHelper.removeCartItem(username, item.getFoodId());
                        refresh();
                        if (onDataChanged != null) onDataChanged.run();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        row.addView(btnDel);

        return row;
    }

    private Button makeSmallBtn(String text) {
        Button b = new Button(activity);
        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(0xFF2D2D2D);
        b.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        b.setBackgroundColor(0xFFEEEEEE);
        b.setPadding(0, 0, 0, 0);
        b.setIncludeFontPadding(false);
        b.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(44, 44);
        params.setMargins(3, 0, 3, 0);
        params.gravity = Gravity.CENTER;
        b.setLayoutParams(params);
        return b;
    }

    private String formatPrice(double p) {
        if (p == (int) p) return String.valueOf((int) p);
        return String.format(Locale.US, "%.2f", p);
    }
}
