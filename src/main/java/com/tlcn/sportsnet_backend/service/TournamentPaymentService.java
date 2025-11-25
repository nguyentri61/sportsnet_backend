package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.config.VNPayConfig;
import com.tlcn.sportsnet_backend.dto.payment.VNPayCreateResponse;
import com.tlcn.sportsnet_backend.dto.payment.VNPayReturnResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.entity.TournamentPayment;
import com.tlcn.sportsnet_backend.enums.PaymentStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
import com.tlcn.sportsnet_backend.repository.TournamentPaymentRepository;
import com.tlcn.sportsnet_backend.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TournamentPaymentService {

    private final VNPayConfig config;
    private final TournamentPaymentRepository paymentRepo;
    private final TournamentParticipantRepository participantRepo;
    private final AccountRepository accountRepo;
    private final TournamentCategoryRepository categoryRepo;

    // Tạo URL thanh toán
    public VNPayCreateResponse createPayment(String categoryId, Double amount) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepo.findByEmail(auth.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        TournamentCategory category = categoryRepo.findById(categoryId).orElseThrow(() -> new InvalidDataException("Category not found"));

        TournamentParticipant participant = participantRepo.findByAccountAndCategory(account, category);


        String txnRef = String.valueOf(System.currentTimeMillis());

        // Lưu vào DB trước
        TournamentPayment payment = TournamentPayment.builder()
                .participant(participant)
                .txnRef(txnRef)
                .amount(amount)
                .status(PaymentStatusEnum.PENDING)
                .build();

        paymentRepo.save(payment);

        long vnpAmount = Math.round(amount * 100);

        // Build param cho VNPay
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_Amount", vnpAmount  + "");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan le phi thi dau");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // Build query
        String query = VNPayUtil.buildQuery(params);

        // Build hash
        String secureHash = VNPayUtil.hmacSHA512(config.getHashSecret(), query);

        return  VNPayCreateResponse.builder()
                .paymentUrl(config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash)
                .build();
    }

    // Xử lý callback
    public VNPayReturnResponse handleReturn(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");

        TournamentPayment payment = paymentRepo.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatusEnum.SUCCESS);
            payment.setPaidAt(Instant.now());
            payment.setVnpTransactionNo(transactionNo);
            payment.setBankCode(bankCode);
        } else {
            payment.setStatus(PaymentStatusEnum.FAILED);
        }

        paymentRepo.save(payment);


        return VNPayReturnResponse.builder()
                .status(payment.getStatus().toString())
                .build();
    }
}
