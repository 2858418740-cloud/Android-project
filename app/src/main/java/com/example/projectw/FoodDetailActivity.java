package com.example.projectw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * 菜品详情页
 * 展示菜品大图、名称、价格、介绍 + 数量选择器 + 加入购物车
 */
public class FoodDetailActivity extends AppCompatActivity {

    private ImageView ivFoodDetail;
    private TextView tvFoodName, tvFoodPrice, tvFoodDesc;
    private TextView tvQuantity;
    private Button btnMinus, btnPlus;
    private Button btnAddToCart, btnBack;

    private DBHelper dbHelper;
    private String username;
    private int foodId;
    private int quantity = 1; // 默认数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // 获取 Intent 数据
        foodId = getIntent().getIntExtra("food_id", -1);
        username = getIntent().getStringExtra("username");
        dbHelper = new DBHelper(this);

        initView();
        loadFoodDetail();

        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 数量减
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // 数量加
        btnPlus.setOnClickListener(v -> {
            if (quantity < 99) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // 加入购物车
        btnAddToCart.setOnClickListener(v -> {
            Food food = dbHelper.getFoodById(foodId);
            if (food != null) {
                dbHelper.addToCartWithCount(username, food.getId(),
                        food.getFoodName(), food.getPrice(), quantity);
                Toast.makeText(this, "已加入购物车", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initView() {
        ivFoodDetail = findViewById(R.id.iv_food_detail);
        tvFoodName = findViewById(R.id.tv_food_name);
        tvFoodPrice = findViewById(R.id.tv_food_price);
        tvFoodDesc = findViewById(R.id.tv_food_desc);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBack = findViewById(R.id.btn_back);
    }

    /** 加载菜品详情 */
    private void loadFoodDetail() {
        Food food = dbHelper.getFoodById(foodId);
        if (food != null) {
            tvFoodName.setText(food.getFoodName());
            tvFoodPrice.setText(formatPrice(food.getPrice()));
            tvFoodDesc.setText(food.getFoodDesc());

            // 加载菜品图片
            int resId = getResources().getIdentifier(
                    food.getImageRes(), "drawable", getPackageName());
            ivFoodDetail.setImageResource(resId != 0 ? resId : R.drawable.nailong_logo);
        }
    }

    private String formatPrice(double price) {
        if (price == (int) price) return "¥" + (int) price + ".00";
        return "¥" + String.format(Locale.US, "%.2f", price);
    }
}
