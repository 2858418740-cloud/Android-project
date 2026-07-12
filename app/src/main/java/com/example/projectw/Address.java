package com.example.projectw;

/** 收货地址实体 */
public class Address {
    private int id;
    private String username;
    private String detail;
    private int isDefault; // 1=默认地址

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public int getIsDefault() { return isDefault; }
    public void setIsDefault(int isDefault) { this.isDefault = isDefault; }

    public boolean isDefault() { return isDefault == 1; }

    @Override
    public String toString() { return detail; }
}
