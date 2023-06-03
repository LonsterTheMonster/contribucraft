package com.contribucraft.Woo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;

public class WMCProcessedOrders {
    @SerializedName("processedOrders")
    @Expose
    private List<Integer> processedOrders = null;

    public List<Integer> getProcessedOrders() {
        return this.processedOrders;
    }

    public void setProcessedOrders(List<Integer> processedOrders) {
        this.processedOrders = processedOrders;
    }

    public WMCProcessedOrders withProcessedOrders(List<Integer> processedOrders) {
        this.processedOrders = processedOrders;
        return this;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("processedOrders", this.processedOrders).toString();
    }
}
