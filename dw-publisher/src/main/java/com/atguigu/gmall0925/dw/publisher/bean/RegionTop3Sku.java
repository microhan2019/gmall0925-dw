package com.atguigu.gmall0925.dw.publisher.bean;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 这个类 是把mysql中的字段转成一个java对象
 */

@Table(name = "ads_region_top3_sku")   //如果表名和类名相同 ( 驼峰和下划线自动互转)  可以省略这个注释
public class RegionTop3Sku {

    @Id
    @Column //表示这个是对应数据库的一列,如果不一样可以 @Column("数据库列名")使其一致
    String dt ;

    @Column
    String region;

    @Column
    String skuName;

    @Column
    BigDecimal orderAmount;

    @Column
    String provinceRemark;


    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getProvinceRemark() {
        return provinceRemark;
    }

    public void setProvinceRemark(String provinceRemark) {
        this.provinceRemark = provinceRemark;
    }
}
