package com.courtlink.payment.repository;

import com.courtlink.payment.entity.WeChatPayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeChatPayOrderRepository extends JpaRepository<WeChatPayOrder, Long> {

    /**
     * 根据商户订单号查找订单
     */
    Optional<WeChatPayOrder> findByOutTradeNo(String outTradeNo);

    /**
     * 根据微信支付订单号查找订单
     */
    Optional<WeChatPayOrder> findByTransactionId(String transactionId);

    /**
     * 根据支付记录ID查找订单
     */
    List<WeChatPayOrder> findByPaymentId(Long paymentId);

    /**
     * 根据订单状态查找订单
     */
    List<WeChatPayOrder> findByStatus(WeChatPayOrder.OrderStatus status);

    /**
     * 查找指定时间范围内的订单
     */
    @Query("SELECT w FROM WeChatPayOrder w WHERE w.createdTime BETWEEN :startTime AND :endTime")
    List<WeChatPayOrder> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查找已过期但状态仍为待支付的订单
     */
    @Query("SELECT w FROM WeChatPayOrder w WHERE w.status = 'PENDING' AND w.expireTime < :currentTime")
    List<WeChatPayOrder> findExpiredPendingOrders(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找指定时间前创建的待支付订单
     */
    @Query("SELECT w FROM WeChatPayOrder w WHERE w.status = 'PENDING' AND w.createdTime < :beforeTime")
    List<WeChatPayOrder> findPendingOrdersCreatedBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据支付状态和是否模拟订单查找
     */
    List<WeChatPayOrder> findByStatusAndIsMock(WeChatPayOrder.OrderStatus status, Boolean isMock);

    /**
     * 统计指定状态的订单数量
     */
    @Query("SELECT COUNT(w) FROM WeChatPayOrder w WHERE w.status = :status")
    long countByStatus(@Param("status") WeChatPayOrder.OrderStatus status);

    /**
     * 统计指定时间范围内的订单总金额
     */
    @Query("SELECT SUM(w.totalAmount) FROM WeChatPayOrder w WHERE w.status = 'SUCCESS' " +
           "AND w.paidTime BETWEEN :startTime AND :endTime")
    Long sumTotalAmountByPaidTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新过期订单状态
     */
    @Modifying
    @Query("UPDATE WeChatPayOrder w SET w.status = 'EXPIRED', w.updatedTime = :currentTime " +
           "WHERE w.status = 'PENDING' AND w.expireTime < :currentTime")
    int updateExpiredOrders(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 删除指定时间前的已完成订单（用于数据清理）
     */
    @Modifying
    @Query("DELETE FROM WeChatPayOrder w WHERE w.status IN ('SUCCESS', 'FAILED', 'CLOSED', 'EXPIRED') " +
           "AND w.createdTime < :beforeTime")
    int deleteCompletedOrdersBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查找最近的订单
     */
    @Query("SELECT w FROM WeChatPayOrder w ORDER BY w.createdTime DESC")
    List<WeChatPayOrder> findRecentOrders();

    /**
     * 根据商户订单号前缀查找订单
     */
    @Query("SELECT w FROM WeChatPayOrder w WHERE w.outTradeNo LIKE :prefix%")
    List<WeChatPayOrder> findByOutTradeNoStartingWith(@Param("prefix") String prefix);

    /**
     * 检查订单号是否存在
     */
    boolean existsByOutTradeNo(String outTradeNo);

    /**
     * 检查微信支付订单号是否存在
     */
    boolean existsByTransactionId(String transactionId);
} 