package com.example.projectw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 购物车列表 RecyclerView 适配器（支持长按删除）
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemLongClickListener {
        void onLongClick(CartItem item);
    }

    private List<CartItem> cartList;
    private OnCartItemLongClickListener longClickListener;

    public CartAdapter(List<CartItem> cartList, OnCartItemLongClickListener listener) {
        this.cartList = cartList;
        this.longClickListener = listener;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvFoodName.setText(item.getFoodName());
        holder.tvFoodPrice.setText("¥" + item.getPrice());
        holder.tvFoodCount.setText("x" + item.getCount());

        // 长按删除
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodPrice, tvFoodCount;

        CartViewHolder(View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_cart_food_name);
            tvFoodPrice = itemView.findViewById(R.id.tv_cart_food_price);
            tvFoodCount = itemView.findViewById(R.id.tv_cart_food_count);
        }
    }
}
