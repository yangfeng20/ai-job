package com.maple.ai.job.hunting.controller;

import com.maple.ai.job.hunting.common.HeaderContext;
import com.maple.ai.job.hunting.model.common.Response;
import com.maple.ai.job.hunting.model.vo.ProductVO;
import com.maple.ai.job.hunting.service.biz.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author maple
 * Created Date: 2024/6/5 17:14
 * Description:
 */

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @PostMapping("/user/product/list")
    public Response<List<ProductVO>> getUserProductList() throws Exception {
        Long userId = HeaderContext.getHeader().getUserId();
        return Response.success(productService.getUserProductList(userId));
    }
}
