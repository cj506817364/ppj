package com.ppj.test.server;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-26 21:06
 * Description:
 */

public class Goods implements java.io.Serializable {
    private String goodsName;
    private Long goodsId;
    private String desc;
    private String shopInfo;

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getShopInfo() {
        return shopInfo;
    }

    public void setShopInfo(String shopInfo) {
        this.shopInfo = shopInfo;
    }
}