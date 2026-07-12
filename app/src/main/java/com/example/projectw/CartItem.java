package com.example.projectw;

/**
 * 购物车条目实体类
 */
public class CartItem {
    private int id;
    private String username;
    private int foodId;
    private String foodName;
    private double price;
    private int count;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
