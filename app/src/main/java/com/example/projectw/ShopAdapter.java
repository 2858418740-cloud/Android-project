package com.example.projectw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * 店铺列表适配器（AI 推荐头部 + 店铺条目）
 */
public class ShopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_AI_HEADER = 0;
    private static final int TYPE_SHOP = 1;

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    public interface OnAiActionListener {
        void onRefresh();     // 换一换
        void onChat();        // 对话
        void onAdopt();       // 一键采纳
    }

    private List<Shop> shopList;
    private final OnShopClickListener shopListener;
    private final OnAiActionListener aiListener;
    private String aiRecommendText = "加载中...";

    public ShopAdapter(List<Shop> shopList, OnShopClickListener shopListener, OnAiActionListener aiListener) {
        this.shopList = shopList;
        this.shopListener = shopListener;
        this.aiListener = aiListener;
    }

    public void updateList(List<Shop> newList) {
        this.shopList = newList;
        notifyDataSetChanged();
    }

    public void updateAiText(String text) {
        this.aiRecommendText = text;
        notifyItemChanged(0);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_AI_HEADER : TYPE_SHOP;
    }

    @Override
    public int getItemCount() {
        return shopList.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_AI_HEADER) {
            return new AiHeaderViewHolder(inflater.inflate(R.layout.item_ai_header, parent, false));
        }
        return new ShopViewHolder(inflater.inflate(R.layout.item_shop, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AiHeaderViewHolder) {
            AiHeaderViewHolder h = (AiHeaderViewHolder) holder;
            h.tvRecommend.setText(aiRecommendText);
            h.btnChange.setOnClickListener(v -> {
                h.tvRecommend.setText("正在推荐...");
                aiListener.onRefresh();
            });
            h.btnChat.setOnClickListener(v -> aiListener.onChat());
            h.btnAdopt.setOnClickListener(v -> aiListener.onAdopt());
        } else {
            ShopViewHolder h = (ShopViewHolder) holder;
            Shop shop = shopList.get(position - 1);

            h.tvShopName.setText(shop.getShopName());
            h.tvRating.setText("⭐" + String.format(Locale.US, "%.1f", shop.getRating()));
            h.tvSales.setText("月售" + shop.getSaleCount());
            h.tvDeliveryTime.setText("约" + shop.getDeliveryTime());
            h.tvDistance.setText(shop.getDistance());
            h.tvRank.setText("🏅第" + shop.getLocalRank() + "名");
            h.tvDineType.setText(shop.getDineType());

            if (shop.hasPromotion()) {
                h.tvPromotion.setText("🏷️ " + shop.getPromotion());
                h.tvPromotion.setVisibility(View.VISIBLE);
            } else {
                h.tvPromotion.setVisibility(View.GONE);
            }

            // 加载店铺 Logo
            int resId = h.itemView.getContext().getResources().getIdentifier(
                    shop.getShopLogo(), "drawable",
                    h.itemView.getContext().getPackageName());
            h.ivLogo.setImageResource(resId != 0 ? resId : R.drawable.nailong_logo);

            h.itemView.setOnClickListener(v -> shopListener.onShopClick(shop));
        }
    }

    // ---- ViewHolders ----

    static class AiHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecommend;
        Button btnChat, btnChange, btnAdopt;

        AiHeaderViewHolder(View itemView) {
            super(itemView);
            tvRecommend = itemView.findViewById(R.id.tv_ai_recommend);
            btnChat = itemView.findViewById(R.id.btn_ai_chat);
            btnChange = itemView.findViewById(R.id.btn_ai_change);
            btnAdopt = itemView.findViewById(R.id.btn_ai_adopt);
        }
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvShopName, tvRating, tvSales, tvDeliveryTime;
        TextView tvDistance, tvRank, tvDineType, tvPromotion;

        ShopViewHolder(View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.iv_shop_logo);
            tvShopName = itemView.findViewById(R.id.tv_shop_name);
            tvRating = itemView.findViewById(R.id.tv_shop_rating);
            tvSales = itemView.findViewById(R.id.tv_shop_sales);
            tvDeliveryTime = itemView.findViewById(R.id.tv_shop_delivery_time);
            tvDistance = itemView.findViewById(R.id.tv_shop_distance);
            tvRank = itemView.findViewById(R.id.tv_shop_rank);
            tvDineType = itemView.findViewById(R.id.tv_shop_dine_type);
            tvPromotion = itemView.findViewById(R.id.tv_shop_promotion);
        }
    }
}
