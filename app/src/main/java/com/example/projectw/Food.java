package com.example.projectw;

/**
 * 菜品实体类
 */
public class Food {
    private int id;
    private int shopId;
    private String foodName;
    private double price;
    private String foodDesc;
    private String imageRes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getFoodDesc() { return foodDesc; }
    public void setFoodDesc(String foodDesc) { this.foodDesc = foodDesc; }

    public String getImageRes() { return imageRes; }
    public void setImageRes(String imageRes) { this.imageRes = imageRes; }
}
