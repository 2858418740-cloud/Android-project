package com.example.projectw;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * 订单历史页
 */
public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderList;
    private DBHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        username = getIntent().getStringExtra("username");
        dbHelper = new DBHelper(this);

        rvOrderList = findViewById(R.id.rv_order_list);
        rvOrderList.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = dbHelper.getOrdersByUser(username);
        rvOrderList.setAdapter(new OrderAdapter(orders));
    }

    // ---- 内联适配器 ----
    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {
        private final List<Order> orders;

        OrderAdapter(List<Order> orders) { this.orders = orders; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false));
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Order o = orders.get(pos);
            h.tvTime.setText(o.getOrderTime());
            h.tvTotal.setText(formatPrice(o.getTotalPrice()));
            h.tvFoods.setText(parseFoodInfo(o.getFoodInfo()));
        }

        @Override public int getItemCount() { return orders.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTime, tvTotal, tvFoods;
            VH(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tv_order_time);
                tvTotal = v.findViewById(R.id.tv_order_total);
                tvFoods = v.findViewById(R.id.tv_order_foods);
            }
        }
    }

    /** 解析 food_info JSON 为可读字符串 */
    private String parseFoodInfo(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.has("type") && obj.getString("type").equals("delivery")) continue; // 跳过配送信息
                String name = obj.getString("name");
                int count = obj.getInt("count");
                if (i > 0 && sb.length() > 0) sb.append("、");
                sb.append(name).append("×").append(count);
            }
            return sb.toString();
        } catch (Exception e) {
            return json;
        }
    }

    private String formatPrice(double p) {
        if (p == (int) p) return "¥" + (int) p + ".00";
        return "¥" + String.format(Locale.US, "%.2f", p);
    }
}
