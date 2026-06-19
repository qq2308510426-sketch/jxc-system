package com.example.jxc.util;

import com.example.jxc.vo.OrderVO;
import com.example.jxc.entity.OrderItem;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrintUtil {

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

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
        html.append("<h2>\u9500\u552e\u8ba2\u5355</h2>");
        html.append("</div>");

        html.append("<div class='info'>");
        html.append("<p>\u8ba2\u5355\u53f7: ").append(escapeHtml(order.getOrderNo())).append("</p>");
        html.append("<p>\u5ba2\u6237\u540d\u79f0: ").append(escapeHtml(order.getCustomerName())).append("</p>");
        html.append("<p>\u521b\u5efa\u65f6\u95f4: ").append(order.getCreateTime() != null ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "").append("</p>");
        html.append("</div>");

        html.append("<table>");
        html.append("<tr><th>\u5546\u54c1ID</th><th>\u6570\u91cf</th><th>\u5355\u4ef7</th><th>\u5c0f\u8ba1</th></tr>");

        for (OrderItem item : items) {
            html.append("<tr>");
            html.append("<td>").append(item.getProductId()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(item.getUnitPrice()).append("</td>");
            html.append("<td>").append(item.getSubtotal()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("<p><strong>\u5408\u8ba1\u91d1\u989d: ").append(order.getTotalAmount()).append("</strong></p>");
        html.append("<p>\u5907\u6ce8: ").append(escapeHtml(order.getRemark())).append("</p>");

        html.append("</body></html>");
        return html.toString();
    }
}