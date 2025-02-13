package site.unoeyhi.apd.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import site.unoeyhi.apd.service.AliExpressService;

@RestController
@RequestMapping("/api/crawl")
public class CrawlingController {
    private final AliExpressService aliExpressService;

    public CrawlingController(AliExpressService aliExpressService) {
        this.aliExpressService = aliExpressService;
    }

  @GetMapping("/products")
  public List<String> getCrawledProducts(
      @RequestParam String url,
      @RequestParam(defaultValue = "10") int maxProducts,
      @RequestParam Long adminId // ✅ adminId 추가
  ) {
    return aliExpressService.fetchProductDetails(url, maxProducts, adminId);

  }
}