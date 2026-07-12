package com.example.projectw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * 菜品列表 RecyclerView 适配器（支持头部店铺信息 + 点菜分隔 + 菜品条目三种视图类型）
 */
public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;    // 店铺头部大图+信息
    private static final int TYPE_DIVIDER = 1;   // 点菜分隔线
    private static final int TYPE_FOOD = 2;      // 菜品条目

    public interface OnFoodActionListener {
        void onFoodClick(Food food);
        void onAddClick(Food food);
    }

    private final Context context;
    private final Shop shopInfo;            // 店铺信息（头部用）
    private final List<Food> foodList;
    private final OnFoodActionListener listener;
    private String formatPrice(double price) {
        if (price == (int) price) return "¥" + (int) price + ".00";
        return "¥" + String.format(Locale.US, "%.2f", price);
    }

    public FoodAdapter(Context context, Shop shopInfo, List<Food> foodList, OnFoodActionListener listener) {
        this.context = context;
        this.shopInfo = shopInfo;
        this.foodList = foodList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) return TYPE_DIVIDER;
        return TYPE_FOOD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_shop_header, parent, false));
        } else if (viewType == TYPE_DIVIDER) {
            return new DividerViewHolder(inflater.inflate(R.layout.item_section_divider, parent, false));
        } else {
            return new FoodViewHolder(inflater.inflate(R.layout.item_food, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            bindHeader((HeaderViewHolder) holder);
        } else if (holder instanceof FoodViewHolder) {
            // position 0=header, 1=divider, 2+ = food
            int foodIndex = position - 2;
            bindFood((FoodViewHolder) holder, foodList.get(foodIndex));
        }
        // DividerViewHolder 无需绑定数据
    }

    private void bindHeader(HeaderViewHolder h) {
        h.tvDineType.setText(shopInfo.getDineType());
        h.tvShopName.setText(shopInfo.getShopName());
        h.tvRating.setText("⭐" + String.format(Locale.US, "%.1f", shopInfo.getRating()));
        h.tvSales.setText("月售" + shopInfo.getSaleCount());
        h.tvDeliveryTime.setText("约" + shopInfo.getDeliveryTime());
        h.tvRank.setText("🏅第" + shopInfo.getLocalRank() + "名");

        // 优惠
        if (shopInfo.hasPromotion()) {
            h.tvPromotion.setText("🏷️ " + shopInfo.getPromotion());
            h.tvPromotion.setVisibility(View.VISIBLE);
        } else {
            h.tvPromotion.setVisibility(View.GONE);
        }

        // Banner 大图
        int resId = context.getResources().getIdentifier(
                shopInfo.getShopImage(), "drawable", context.getPackageName());
        h.ivBanner.setImageResource(resId != 0 ? resId : R.drawable.nailong_logo);
    }

    private void bindFood(FoodViewHolder h, Food food) {
        h.tvFoodName.setText(food.getFoodName());
        h.tvFoodPrice.setText(formatPrice(food.getPrice()));

        int resId = context.getResources().getIdentifier(
                food.getImageRes(), "drawable", context.getPackageName());
        h.ivFood.setImageResource(resId != 0 ? resId : R.drawable.nailong_logo);

        h.itemView.setOnClickListener(v -> listener.onFoodClick(food));
        h.btnAdd.setOnClickListener(v -> listener.onAddClick(food));
    }

    @Override
    public int getItemCount() {
        return foodList.size() + 2; // header + divider + foods
    }

    // ---- ViewHolders ----

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvDineType, tvShopName, tvRating, tvSales, tvDeliveryTime, tvRank, tvPromotion;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_shop_banner);
            tvDineType = itemView.findViewById(R.id.tv_header_dine_type);
            tvShopName = itemView.findViewById(R.id.tv_header_shop_name);
            tvRating = itemView.findViewById(R.id.tv_header_rating);
            tvSales = itemView.findViewById(R.id.tv_header_sales);
            tvDeliveryTime = itemView.findViewById(R.id.tv_header_delivery_time);
            tvRank = itemView.findViewById(R.id.tv_header_rank);
            tvPromotion = itemView.findViewById(R.id.tv_header_promotion);
        }
    }

    static class DividerViewHolder extends RecyclerView.ViewHolder {
        DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvFoodName, tvFoodPrice;
        Button btnAdd;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.iv_food);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvFoodPrice = itemView.findViewById(R.id.tv_food_price);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}
