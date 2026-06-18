package com.example.jxc.util;

import com.example.jxc.entity.Order;
import com.example.jxc.entity.OrderItem;
import com.example.jxc.vo.OrderVO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrintUtil {

    public static String generateOrderPrintHtml(OrderVO order, List<OrderItem> items) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: SimSun, serif; font-size: 12px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        html.append("th, td { border: 1px solid #000; padding: 5px; text-align: left; }");
        html.append("th { background-color: #f0f0f0; }");
        html.append(".header { text-align: center; margin-bottom: 20px; }");
        html.append(".info { margin: 10px 0; }");
        html.append("</style></head><body>");

        html.append("<div class='header'>");
        html.append("<h2>销售订单</h2>");
        html.append("</div>");

        html.append("<div class='info'>");
        html.append("<p>订单号: ").append(order.getOrderNo()).append("</p>");
        html.append("<p>客户名称: ").append(order.getCustomerName() != null ? order.getCustomerName() : "").append("</p>");
        html.append("<p>创建时间: ").append(order.getCreateTime() != null ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "").append("</p>");
        html.append("</div>");

        html.append("<table>");
        html.append("<tr><th>商品ID</th><th>数量</th><th>单价</th><th>小计</th></tr>");

        for (OrderItem item : items) {
            html.append("<tr>");
            html.append("<td>").append(item.getProductId()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(item.getUnitPrice()).append("</td>");
            html.append("<td>").append(item.getSubtotal()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("<p><strong>合计金额: ").append(order.getTotalAmount()).append("</strong></p>");
        html.append("<p>备注: ").append(order.getRemark() != null ? order.getRemark() : "").append("</p>");

        html.append("</body></html>");
        return html.toString();
    }
}