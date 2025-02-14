package site.unoeyhi.apd.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasRole('ADMIN')")//관리자만 요청가능
  public List<String> getCrawledProducts(
      @RequestParam String url,
      @RequestParam(defaultValue = "10") int maxProducts
  ) {
    return aliExpressService.fetchProductDetails(url, maxProducts);

  }
}