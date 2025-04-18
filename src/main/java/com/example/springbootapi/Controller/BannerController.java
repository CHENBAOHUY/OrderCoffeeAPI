package com.example.springbootapi.Controller;

import com.example.springbootapi.Service.BannerService;
import com.example.springbootapi.dto.BannerDTO;
import com.example.springbootapi.dto.BannerRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {
    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    private final BannerService bannerService;

    @Autowired
    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<List<BannerDTO>> getActiveBanners() {
        logger.info("Request to get active banners");
        List<BannerDTO> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BannerDTO> getBannerById(@PathVariable Integer id) {
        logger.info("Request to get banner with id: {}", id);
        BannerDTO banner = bannerService.getBannerById(id);
        return ResponseEntity.ok(banner);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BannerDTO> createBanner(@Valid @RequestBody BannerRequest request) {
        logger.info("Request to create banner with title: {}", request.getTitle());
        BannerDTO banner = bannerService.createBanner(request);
        return ResponseEntity.status(201).body(banner);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BannerDTO> updateBanner(@PathVariable Integer id, @Valid @RequestBody BannerRequest request) {
        logger.info("Request to update banner with id: {}", id);
        BannerDTO banner = bannerService.updateBanner(id, request);
        return ResponseEntity.ok(banner);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBanner(@PathVariable Integer id) {
        logger.info("Request to delete banner with id: {}", id);
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("Banner deleted");
    }
}