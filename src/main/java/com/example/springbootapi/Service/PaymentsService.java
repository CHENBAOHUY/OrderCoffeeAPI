package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.OrderDetails;
import com.example.springbootapi.Entity.Orders;
import com.example.springbootapi.Entity.Payments;
import com.example.springbootapi.dto.OrderDetailsDTO;
import com.example.springbootapi.dto.PaymentResultDTO;
import com.example.springbootapi.dto.PaymentsDTO;
import com.example.springbootapi.dto.VnPayCallbackDTO;
import com.example.springbootapi.repository.OrderDetailsRepository;
import com.example.springbootapi.repository.OrdersRepository;
import com.example.springbootapi.repository.PaymentsRepository;
import com.example.springbootapi.repository.ProductsRepository;
import com.example.springbootapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentsService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsService.class);

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductsRepository productsRepository;

    public List<PaymentsDTO> getAllPayments() {
        return paymentsRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<PaymentsDTO> getPaymentById(Integer id) {
        return paymentsRepository.findById(id).map(this::toDTO);
    }

    public List<PaymentsDTO> getPaymentsByOrderId(Integer orderId) {
        return paymentsRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentsDTO addPayment(Payments payment) {
        Payments savedPayment = paymentsRepository.save(payment);
        return toDTO(savedPayment);
    }

    @Transactional
    public PaymentsDTO createPayment(Integer orderId, BigDecimal amount, String paymentMethod, String txnRef) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus(Payments.PaymentStatus.PENDING);
        payment.setTxnRef(txnRef); // Lưu vnp_TxnRef
        Payments savedPayment = paymentsRepository.save(payment);
        return toDTO(savedPayment);
    }

    @Transactional
    public void deletePayment(Integer id) {
        paymentsRepository.deleteById(id);
    }

    @Transactional
    public PaymentResultDTO processVnPayCallback(VnPayCallbackDTO callbackData) {
        try {
            // Kiểm tra tham số bắt buộc
            if (callbackData.getVnp_TxnRef() == null || callbackData.getVnp_Amount() == null || callbackData.getVnp_ResponseCode() == null) {
                logger.error("Invalid callback data: missing required fields - vnp_TxnRef={}, vnp_Amount={}, vnp_ResponseCode={}",
                        callbackData.getVnp_TxnRef(), callbackData.getVnp_Amount(), callbackData.getVnp_ResponseCode());
                return PaymentResultDTO.builder()
                        .success(false)
                        .message("Dữ liệu callback không hợp lệ: thiếu các trường bắt buộc")
                        .build();
            }

            // Tìm Payments theo vnp_TxnRef
            Optional<Payments> paymentOpt = paymentsRepository.findByTxnRef(callbackData.getVnp_TxnRef());
            if (!paymentOpt.isPresent()) {
                logger.error("Payment not found with vnp_TxnRef: {}", callbackData.getVnp_TxnRef());
                return PaymentResultDTO.builder()
                        .success(false)
                        .message("Không tìm thấy giao dịch với vnp_TxnRef: " + callbackData.getVnp_TxnRef())
                        .build();
            }

            Payments payment = paymentOpt.get();
            Integer orderId = payment.getOrder().getId();

            // Tìm đơn hàng
            Orders order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.error("Order not found with ID: {}", orderId);
                        return new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
                    });

            // Kiểm tra xem đơn hàng đã có giao dịch COMPLETED nào chưa
            List<Payments> existingPayments = paymentsRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
            Optional<Payments> completedPayment = existingPayments.stream()
                    .filter(p -> p.getStatus() == Payments.PaymentStatus.COMPLETED)
                    .findFirst();

            if (completedPayment.isPresent()) {
                logger.warn("Order {} already has a completed payment", orderId);
                return PaymentResultDTO.builder()
                        .success(true)
                        .orderId(orderId)
                        .message("Đơn hàng đã được thanh toán trước đó")
                        .transactionId(callbackData.getVnp_TransactionNo())
                        .paymentMethod("VNPAY")
                        .bankCode(callbackData.getVnp_BankCode())
                        .cardType(callbackData.getVnp_CardType())
                        .build();
            }

            // Cập nhật thông tin thanh toán
            try {
                payment.setAmount(new BigDecimal(callbackData.getVnp_Amount()).divide(new BigDecimal(100)));
                logger.info("Parsed vnp_Amount: {} VND", payment.getAmount());
            } catch (NumberFormatException e) {
                logger.error("Invalid vnp_Amount: {}", callbackData.getVnp_Amount());
                return PaymentResultDTO.builder()
                        .success(false)
                        .message("Số tiền không hợp lệ")
                        .build();
            }

            payment.setTransactionId(callbackData.getVnp_TransactionNo());
            payment.setResponseCode(callbackData.getVnp_ResponseCode());
            payment.setBankCode(callbackData.getVnp_BankCode());
            payment.setCardType(callbackData.getVnp_CardType());

            // Parse vnp_PayDate
            if (callbackData.getVnp_PayDate() != null && !callbackData.getVnp_PayDate().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    payment.setPaymentDate(LocalDateTime.parse(callbackData.getVnp_PayDate(), formatter));
                    logger.info("Parsed vnp_PayDate: {}", payment.getPaymentDate());
                } catch (Exception e) {
                    logger.warn("Failed to parse vnp_PayDate: {}, using current time", callbackData.getVnp_PayDate());
                    payment.setPaymentDate(LocalDateTime.now());
                }
            } else {
                logger.warn("vnp_PayDate is null or empty, using current time");
                payment.setPaymentDate(LocalDateTime.now());
            }

            // Xử lý trạng thái giao dịch
            boolean isSuccess = "00".equals(callbackData.getVnp_ResponseCode());
            if (isSuccess) {
                payment.setStatus(Payments.PaymentStatus.COMPLETED);
                order.setStatus(Orders.OrderStatus.COMPLETED);
                logger.info("Payment successful for order {}: status set to COMPLETED", orderId);

                // Xóa các bản ghi PENDING khác liên quan đến orderId
                List<Payments> pendingPayments = existingPayments.stream()
                        .filter(p -> p.getStatus() == Payments.PaymentStatus.PENDING)
                        .collect(Collectors.toList());
                for (Payments pending : pendingPayments) {
                    if (!pending.getId().equals(payment.getId())) { // Không xóa bản ghi hiện tại
                        paymentsRepository.delete(pending);
                        logger.info("Deleted redundant PENDING payment with ID {} for order {}", pending.getId(), orderId);
                    }
                }
            } else {
                payment.setStatus(Payments.PaymentStatus.FAILED);
                order.setStatus(Orders.OrderStatus.CANCELLED);
                logger.info("Payment failed for order {}: status set to CANCELLED", orderId);
            }

            String responseMessage = getVnPayResponseMessage(callbackData.getVnp_ResponseCode());
            payment.setResponseMessage(responseMessage);

            // Lưu payment và order
            paymentsRepository.save(payment);
            ordersRepository.save(order);

            logger.info("Processed VNPay callback for order {}: success={}, message={}", orderId, isSuccess, responseMessage);

            return PaymentResultDTO.builder()
                    .success(isSuccess)
                    .orderId(orderId)
                    .message(responseMessage)
                    .transactionId(callbackData.getVnp_TransactionNo())
                    .paymentMethod("VNPAY")
                    .bankCode(callbackData.getVnp_BankCode())
                    .cardType(callbackData.getVnp_CardType())
                    .paymentTime(payment.getPaymentDate())
                    .build();

        } catch (Exception e) {
            logger.error("Error processing VNPay callback: {}", e.getMessage(), e);
            return PaymentResultDTO.builder()
                    .success(false)
                    .message("Lỗi xử lý thanh toán: " + e.getMessage())
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public PaymentResultDTO getPaymentResult(Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        List<Payments> payments = paymentsRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        Payments payment = payments.stream()
                .filter(p -> p.getStatus() == Payments.PaymentStatus.COMPLETED)
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("No completed payment found for order: {}", orderId);
                    return new RuntimeException("Không tìm thấy thông tin thanh toán hoàn tất cho đơn hàng: " + orderId);
                });

        List<OrderDetails> orderDetails = orderDetailsRepository.findByOrderId(orderId);
        List<OrderDetailsDTO> orderDetailsDTO = orderDetails.stream()
                .map(detail -> OrderDetailsDTO.builder()
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .itemTotalPrice(detail.getItemTotalPrice())
                        .build())
                .collect(Collectors.toList());

        if (order.getUser() == null) {
            logger.error("User not found for order: {}", orderId);
            throw new RuntimeException("Không tìm thấy thông tin người dùng cho đơn hàng: " + orderId);
        }

        return PaymentResultDTO.builder()
                .success(payment.getStatus() == Payments.PaymentStatus.COMPLETED)
                .orderId(orderId)
                .message(payment.getResponseMessage() != null ? payment.getResponseMessage() : "Thanh toán thành công")
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .bankCode(payment.getBankCode())
                .cardType(payment.getCardType())
                .paymentTime(payment.getPaymentDate())
                .customerName(order.getUser().getName())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhone())
                .orderItems(orderDetailsDTO)
                .build();
    }

    private String getVnPayResponseMessage(String responseCode) {
        if (responseCode == null) {
            return "Mã phản hồi không hợp lệ";
        }
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13":
                return "Giao dịch không thành công do: Quý khách nhập sai mật khẩu xác thực giao dịch";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99":
                return "Các lỗi khác";
            default:
                return "Lỗi không xác định: " + responseCode;
        }
    }

    private PaymentsDTO toDTO(Payments payment) {
        return new PaymentsDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaymentDate(),
                payment.getTransactionId(),
                payment.getResponseCode(),
                payment.getResponseMessage(),
                payment.getBankCode(),
                payment.getCardType(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}