package com.tlcn.sportsnet_backend.controller;

import com.tlcn.sportsnet_backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @GetMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam(required = false) String tournamentId,
            @RequestParam(required = false) String accountId,
            @RequestParam("amount") Long amount
    ) throws UnsupportedEncodingException {

        String orderType = "tournament_fee";
        String txnRef = String.valueOf(System.currentTimeMillis());

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Tournament fee payment");
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);

        // Tạo chuỗi để ký
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String field : fieldNames) {
            String value = vnpParams.get(field);
            if (value != null && !value.isEmpty()) {
                hashData.append(field).append("=").append(URLEncoder.encode(value, "UTF-8"));
                query.append(field).append("=").append(URLEncoder.encode(value, "UTF-8"));
                if (!field.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        // Ký SHA512
        String secureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        String paymentUrl = vnp_PayUrl + "?" + query.toString();
        return ResponseEntity.ok(paymentUrl);
    }


    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {

        Map<String, String> fields = new HashMap<>();

        for (Enumeration<?> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String key = (String) params.nextElement();
            String value = request.getParameter(key);

            if (!key.equals("vnp_SecureHash")) {
                fields.put(key, value);
            }
        }

        // Lấy hash từ VNPay trả về
        String secureHash = request.getParameter("vnp_SecureHash");

        // Tạo lại chuỗi ký để verify
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String field : fieldNames) {
            hashData.append(field).append("=").append(fields.get(field));
            if (!field.equals(fieldNames.get(fieldNames.size() - 1))) {
                hashData.append("&");
            }
        }

        String checkHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());

        if (checkHash.equals(secureHash)) {
            // Thanh toán thành công
            String responseCode = request.getParameter("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                return ResponseEntity.ok("PAYMENT SUCCESS");
            } else {
                return ResponseEntity.ok("PAYMENT FAILED");
            }

        } else {
            return ResponseEntity.badRequest().body("INVALID SIGNATURE");
        }
    }

}
