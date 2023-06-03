package com.contribucraft.Woo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Order {
    @SerializedName("player")
    @Expose
    private String player;

    @SerializedName("order_id")
    @Expose
    private Integer orderId;

    @SerializedName("commands")
    @Expose
    private List<String> commands = null;

    public String getPlayer() {
        return this.player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Order withPlayer(String player) {
        this.player = player;
        return this;
    }

    public Integer getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Order withOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public Order withCommands(List<String> commands) {
        this.commands = commands;
        return this;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("player", this.player).append("orderId", this.orderId).append("commands", this.commands).toString();
    }
}