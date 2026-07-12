package com.example.projectw;

/** 订单实体 */
public class Order {
    private int id;
    private String username;
    private double totalPrice;
    private String orderTime;
    private String foodInfo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public String getFoodInfo() { return foodInfo; }
    public void setFoodInfo(String foodInfo) { this.foodInfo = foodInfo; }
}
