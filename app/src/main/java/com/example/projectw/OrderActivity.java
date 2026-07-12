package com.example.projectw;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订单确认页
 * 配送信息 + 购物车列表 + 提交后显示支付二维码
 */
public class OrderActivity extends AppCompatActivity {

    private EditText etDeliveryTime;
    private EditText etDeliveryAddress;
    private RecyclerView rvCartList;
    private TextView tvOrderTotal;
    private TextView tvOrderAmountDisplay;
    private TextView tvSubtotal, tvDeliveryFee, tvPackagingFee;
    private Button btnSubmitOrder;
    private Button btnBack;
    private Button btnSelectAddress;
    private CheckBox cbSaveAddress;
    private CardView cardQrCode;
    private ImageView ivQrCode;
    private LinearLayout layoutBottomBar;
    private CardView layoutDeliveryInfo;

    private DBHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        username = getIntent().getStringExtra("username");
        dbHelper = new DBHelper(this);

        initView();
        loadCartList();

        btnBack.setOnClickListener(v -> finish());

        // 选择常用地址
        btnSelectAddress.setOnClickListener(v -> showAddressPicker());

        // 提交订单 → 显示支付二维码
        btnSubmitOrder.setOnClickListener(v -> submitOrder());
    }

    private void initView() {
        etDeliveryTime = findViewById(R.id.et_delivery_time);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        rvCartList = findViewById(R.id.rv_cart_list);
        tvOrderTotal = findViewById(R.id.tv_order_total);
        tvOrderAmountDisplay = findViewById(R.id.tv_order_amount_display);
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
        btnBack = findViewById(R.id.btn_back);
        btnSelectAddress = findViewById(R.id.btn_select_address);
        cbSaveAddress = findViewById(R.id.cb_save_address);
        cardQrCode = findViewById(R.id.card_qr_code);
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvPackagingFee = findViewById(R.id.tv_packaging_fee);
        layoutBottomBar = findViewById(R.id.layout_bottom_bar);
        layoutDeliveryInfo = findViewById(R.id.layout_delivery_info);

        rvCartList.setLayoutManager(new LinearLayoutManager(this));
    }

    /** 加载购物车 */
    private void loadCartList() {
        List<CartItem> cartList = dbHelper.getCartByUser(username);

        if (cartList.isEmpty()) {
            Toast.makeText(this, "购物车为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CartAdapter adapter = new CartAdapter(cartList, item -> {
            new AlertDialog.Builder(OrderActivity.this)
                    .setTitle("删除菜品")
                    .setMessage("确定要删除「" + item.getFoodName() + "」吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dbHelper.removeCartItem(username, item.getFoodId());
                        loadCartList();
                        Toast.makeText(OrderActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        rvCartList.setAdapter(adapter);

        // 费用明细
        double subtotal = dbHelper.getCartTotalPrice(username);
        int itemCount = dbHelper.getCartTotalCount(username);
        double deliveryFee = 3.0;
        double packagingFee = itemCount * 1.0;
        double total = subtotal + deliveryFee + packagingFee;

        tvSubtotal.setText(formatPrice(subtotal));
        tvDeliveryFee.setText(formatPrice(deliveryFee));
        tvPackagingFee.setText(formatPrice(packagingFee));
        tvOrderTotal.setText(formatPrice(total));
    }

    /** 提交订单 */
    private void submitOrder() {
        // 校验配送信息
        String deliveryTime = etDeliveryTime.getText().toString().trim();
        String deliveryAddress = etDeliveryAddress.getText().toString().trim();

        if (deliveryTime.isEmpty()) {
            Toast.makeText(this, "请填写期望配送时间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (deliveryAddress.isEmpty()) {
            Toast.makeText(this, "请填写配送地址", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartList = dbHelper.getCartByUser(username);
        if (cartList.isEmpty()) {
            Toast.makeText(this, "购物车为空", Toast.LENGTH_SHORT).show();
            return;
        }

        double subtotal = dbHelper.getCartTotalPrice(username);
        int itemCount = dbHelper.getCartTotalCount(username);
        double totalPrice = subtotal + 3.0 + itemCount * 1.0; // 含配送费+打包费
        String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 构建菜品 + 配送信息 JSON
        StringBuilder sb = new StringBuilder("[");
        sb.append("{\"type\":\"delivery\",\"time\":\"").append(deliveryTime)
                .append("\",\"address\":\"").append(deliveryAddress).append("\"}");
        for (CartItem item : cartList) {
            sb.append(",{\"name\":\"").append(item.getFoodName())
                    .append("\",\"count\":").append(item.getCount())
                    .append(",\"price\":").append(item.getPrice()).append("}");
        }
        sb.append("]");
        String foodInfo = sb.toString();

        // 写入订单表
        dbHelper.createOrder(username, totalPrice, orderTime, foodInfo);

        // 清空购物车
        dbHelper.clearCartByUser(username);

        // 如果勾选保存地址
        if (cbSaveAddress.isChecked() && !deliveryAddress.isEmpty()) {
            dbHelper.saveAddress(username, deliveryAddress);
        }

        // 显示支付二维码
        showPaymentQR(totalPrice);
    }

    /** 显示支付二维码（含运费打包费） */
    private void showPaymentQR(double totalPrice) {
        layoutDeliveryInfo.setVisibility(View.GONE);
        layoutBottomBar.setVisibility(View.GONE);
        cardQrCode.setVisibility(View.VISIBLE);
        tvOrderAmountDisplay.setText("支付金额：¥" + formatPrice(totalPrice));

        int resId = getResources().getIdentifier("qr_pay", "drawable", getPackageName());
        if (resId != 0) {
            ivQrCode.setImageResource(resId);
        }

        Toast.makeText(this, "下单成功！请扫码支付", Toast.LENGTH_LONG).show();
    }

    /** 弹出常用地址列表 */
    private void showAddressPicker() {
        List<Address> addresses = dbHelper.getAddressesByUser(username);
        if (addresses.isEmpty()) {
            Toast.makeText(this, "暂无常用地址，请先输入地址并勾选「保存为常用地址」", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            items[i] = addresses.get(i).getDetail();
        }

        new AlertDialog.Builder(this)
                .setTitle("选择常用地址")
                .setItems(items, (dialog, which) -> {
                    etDeliveryAddress.setText(addresses.get(which).getDetail());
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private String formatPrice(double price) {
        if (price == (int) price) return "¥" + (int) price + ".00";
        return "¥" + String.format(Locale.US, "%.2f", price);
    }
}
