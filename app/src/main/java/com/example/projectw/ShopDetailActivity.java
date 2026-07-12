package com.example.projectw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * 店铺详情页
 * 头部：店铺大图 + 信息（随滚动消失）
 * 分隔：点菜
 * 列表：菜品
 * 底部：购物车长条栏
 * 滚动时：工具栏显示"点餐"
 */
public class ShopDetailActivity extends AppCompatActivity {

    private TextView tvToolbarTitle;
    private TextView tvToolbarDiancan;
    private RecyclerView rvFoodList;
    private TextView tvCartCount;
    private TextView tvCartTotal;
    private LinearLayout layoutCartBar;
    private Button btnBack;
    private CartPanelManager cartPanel;

    private DBHelper dbHelper;
    private String username;
    private int shopId;
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        shopId = getIntent().getIntExtra("shop_id", -1);
        String shopName = getIntent().getStringExtra("shop_name");
        username = getIntent().getStringExtra("username");
        dbHelper = new DBHelper(this);

        initView();

        Shop shop = dbHelper.getShopById(shopId);
        List<Food> foodList = dbHelper.getFoodsByShopId(shopId);

        tvToolbarTitle.setText(shopName);

        // 适配器：头部店铺信息 + 分隔 + 菜品列表
        foodAdapter = new FoodAdapter(this, shop, foodList, new FoodAdapter.OnFoodActionListener() {
            @Override
            public void onFoodClick(Food food) {
                Intent intent = new Intent(ShopDetailActivity.this, FoodDetailActivity.class);
                intent.putExtra("food_id", food.getId());
                intent.putExtra("username", username);
                startActivity(intent);
            }

            @Override
            public void onAddClick(Food food) {
                dbHelper.addToCart(username, food.getId(), food.getFoodName(), food.getPrice());
                Toast.makeText(ShopDetailActivity.this, "已加入购物车", Toast.LENGTH_SHORT).show();
                refreshCartBar();
            }
        });

        rvFoodList.setAdapter(foodAdapter);

        // 滚动监听：头部滚出屏幕后右上角显示"点餐"
        rvFoodList.addOnScrollListener(new ShopScrollListener());

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

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

    /** 命名内部类：避免 D8 在 JDK 25 上对匿名类 NPE */
    private class ShopScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (lm != null) {
                int firstVisible = lm.findFirstCompletelyVisibleItemPosition();
                tvToolbarDiancan.setVisibility(firstVisible > 0 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void initView() {
        tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        tvToolbarDiancan = findViewById(R.id.tv_toolbar_diancan);
        rvFoodList = findViewById(R.id.rv_food_list);
        tvCartCount = findViewById(R.id.tv_cart_count);
        tvCartTotal = findViewById(R.id.tv_cart_total);
        layoutCartBar = findViewById(R.id.layout_cart_bar);
        btnBack = findViewById(R.id.btn_back);

        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshCartBar() {
        int count = dbHelper.getCartTotalCount(username);
        double total = dbHelper.getCartTotalPrice(username);
        tvCartCount.setText(count + " 份");
        tvCartTotal.setText(formatPrice(total));
    }

    private String formatPrice(double price) {
        if (price == (int) price) return String.format(Locale.US, "%.2f", price);
        return String.format(Locale.US, "%.2f", price);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBar();
    }
}
