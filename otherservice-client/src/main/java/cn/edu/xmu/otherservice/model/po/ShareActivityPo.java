package cn.edu.xmu.otherservice.model.po;

import java.time.LocalDateTime;

public class ShareActivityPo {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.id
     *
     * @mbg.generated
     */
    private Long id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.shop_id
     *
     * @mbg.generated
     */
    private Long shopId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.goods_spu_id
     *
     * @mbg.generated
     */
    private Long goodsSpuId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.begin_time
     *
     * @mbg.generated
     */
    private LocalDateTime beginTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.end_time
     *
     * @mbg.generated
     */
    private LocalDateTime endTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.strategy
     *
     * @mbg.generated
     */
    private String strategy;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.be_deleted
     *
     * @mbg.generated
     */
    private Byte beDeleted;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.gmt_created
     *
     * @mbg.generated
     */
    private LocalDateTime gmtCreated;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column share_activity.gmt_modified
     *
     * @mbg.generated
     */
    private LocalDateTime gmtModified;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.id
     *
     * @return the value of share_activity.id
     *
     * @mbg.generated
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.id
     *
     * @param id the value for share_activity.id
     *
     * @mbg.generated
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.shop_id
     *
     * @return the value of share_activity.shop_id
     *
     * @mbg.generated
     */
    public Long getShopId() {
        return shopId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.shop_id
     *
     * @param shopId the value for share_activity.shop_id
     *
     * @mbg.generated
     */
    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.goods_spu_id
     *
     * @return the value of share_activity.goods_spu_id
     *
     * @mbg.generated
     */
    public Long getGoodsSpuId() {
        return goodsSpuId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.goods_spu_id
     *
     * @param goodsSpuId the value for share_activity.goods_spu_id
     *
     * @mbg.generated
     */
    public void setGoodsSpuId(Long goodsSpuId) {
        this.goodsSpuId = goodsSpuId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.begin_time
     *
     * @return the value of share_activity.begin_time
     *
     * @mbg.generated
     */
    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.begin_time
     *
     * @param beginTime the value for share_activity.begin_time
     *
     * @mbg.generated
     */
    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.end_time
     *
     * @return the value of share_activity.end_time
     *
     * @mbg.generated
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.end_time
     *
     * @param endTime the value for share_activity.end_time
     *
     * @mbg.generated
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.strategy
     *
     * @return the value of share_activity.strategy
     *
     * @mbg.generated
     */
    public String getStrategy() {
        return strategy;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.strategy
     *
     * @param strategy the value for share_activity.strategy
     *
     * @mbg.generated
     */
    public void setStrategy(String strategy) {
        this.strategy = strategy == null ? null : strategy.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.be_deleted
     *
     * @return the value of share_activity.be_deleted
     *
     * @mbg.generated
     */
    public Byte getBeDeleted() {
        return beDeleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.be_deleted
     *
     * @param beDeleted the value for share_activity.be_deleted
     *
     * @mbg.generated
     */
    public void setBeDeleted(Byte beDeleted) {
        this.beDeleted = beDeleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.gmt_created
     *
     * @return the value of share_activity.gmt_created
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtCreated() {
        return gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.gmt_created
     *
     * @param gmtCreated the value for share_activity.gmt_created
     *
     * @mbg.generated
     */
    public void setGmtCreated(LocalDateTime gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column share_activity.gmt_modified
     *
     * @return the value of share_activity.gmt_modified
     *
     * @mbg.generated
     */
    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column share_activity.gmt_modified
     *
     * @param gmtModified the value for share_activity.gmt_modified
     *
     * @mbg.generated
     */
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }
}