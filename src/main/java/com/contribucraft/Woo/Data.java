package com.contribucraft.Woo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Data {
    @SerializedName("status")
    @Expose
    private Integer status;

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Data withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String toString() {
        return (new ToStringBuilder(this)).append("status", this.status).toString();
    }
}