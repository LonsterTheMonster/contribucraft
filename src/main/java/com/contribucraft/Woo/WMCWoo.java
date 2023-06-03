package com.contribucraft.Woo;

import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;
        import java.util.List;
        import org.apache.commons.lang.builder.ToStringBuilder;

public class WMCWoo {
    @SerializedName("code")
    @Expose
    private String code;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private Data data;

    @SerializedName("orders")
    @Expose
    private List<Order> orders = null;

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public WMCWoo withCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WMCWoo withMessage(String message) {
        this.message = message;
        return this;
    }

    public Data getData() {
        return this.data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public WMCWoo withData(Data data) {
        this.data = data;
        return this;
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public WMCWoo withOrders(List<Order> orders) {
        this.orders = orders;
        return this;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("code", this.code).append("message", this.message).append("data", this.data).append("orders", this.orders).toString();
    }
}
