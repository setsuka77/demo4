package com.example.demo.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrdersForm {
	private int orderId;            // 注文ID
    private BigDecimal totalAmount; // 合計金額
    private int userId;             // ユーザーID
    private LocalDateTime purchaseDate; // 購入日時
    private String purchaseStatus;  // 購入状態
    private String shippingStatus;  // 配送状態
    private String couponId;        // クーポンID（使用された場合）
    
    private List<OrderSummaryForm> orderSummaryList;

}
