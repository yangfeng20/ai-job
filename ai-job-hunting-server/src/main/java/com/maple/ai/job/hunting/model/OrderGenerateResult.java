package com.maple.ai.job.hunting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maple
 * Created Date: 2024/5/8 21:48
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderGenerateResult {

    private Long orderId;

    private String qrCode;

    private byte[] qrCodeBytes;
}
