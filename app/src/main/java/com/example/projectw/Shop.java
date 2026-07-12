package com.example.projectw;

/**
 * 店铺实体类
 */
public class Shop {
    private int id;
    private String shopName;
    private String shopLogo;
    private int saleCount;
    private double deliveryPrice;
    private String deliveryTime;
    private String shopDesc;
    // 新增字段
    private String distance;      // 距离，如 "1.2km"
    private int localRank;        // 当地排名，如 3 → 第3名
    private String dineType;      // 堂食/外卖/堂食&外卖
    private double rating;        // 评分，如 4.8
    private String promotion;     // 优惠信息，如 "满30减5"（空字符串表示无优惠）
    private String shopImage;     // 店铺大图（详情页顶部 banner）

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getShopLogo() { return shopLogo; }
    public void setShopLogo(String shopLogo) { this.shopLogo = shopLogo; }

    public int getSaleCount() { return saleCount; }
    public void setSaleCount(int saleCount) { this.saleCount = saleCount; }

    public double getDeliveryPrice() { return deliveryPrice; }
    public void setDeliveryPrice(double deliveryPrice) { this.deliveryPrice = deliveryPrice; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public String getShopDesc() { return shopDesc; }
    public void setShopDesc(String shopDesc) { this.shopDesc = shopDesc; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public int getLocalRank() { return localRank; }
    public void setLocalRank(int localRank) { this.localRank = localRank; }

    public String getDineType() { return dineType; }
    public void setDineType(String dineType) { this.dineType = dineType; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getPromotion() { return promotion; }
    public void setPromotion(String promotion) { this.promotion = promotion; }

    public String getShopImage() { return shopImage; }
    public void setShopImage(String shopImage) { this.shopImage = shopImage; }

    /** 优惠是否有效（非空字符串表示有优惠） */
    public boolean hasPromotion() {
        return promotion != null && !promotion.isEmpty();
    }
}
