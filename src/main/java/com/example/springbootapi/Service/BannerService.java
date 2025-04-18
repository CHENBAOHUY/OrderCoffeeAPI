package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Banner;
import com.example.springbootapi.dto.BannerDTO;
import com.example.springbootapi.dto.BannerRequest;
import com.example.springbootapi.repository.BannerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {
    private static final Logger logger = LoggerFactory.getLogger(BannerService.class);

    private final BannerRepository bannerRepository;

    @Autowired
    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<BannerDTO> getActiveBanners() {
        logger.info("Fetching active banners");
        return bannerRepository.findActiveBanners().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BannerDTO getBannerById(Integer id) {
        logger.info("Fetching banner with id: {}", id);
        Banner banner = bannerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found or deleted with id: " + id));
        return convertToDTO(banner);
    }

    @Transactional
    public BannerDTO createBanner(BannerRequest request) {
        logger.info("Creating new banner with title: {}", request.getTitle());
        validateUrl(request.getImageUrl()); // Thêm kiểm tra URL

        Banner banner = new Banner();
        banner.setTitle(request.getTitle());
        banner.setImage(request.getImageUrl());
        banner.setIsActive(request.getIsActive());
        banner.setIsDeleted(false);

        banner = bannerRepository.save(banner);
        return convertToDTO(banner);
    }

    @Transactional
    public BannerDTO updateBanner(Integer id, BannerRequest request) {
        logger.info("Updating banner with id: {}", id);
        validateUrl(request.getImageUrl()); // Thêm kiểm tra URL

        Banner banner = bannerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found or deleted with id: " + id));
        banner.setTitle(request.getTitle());
        banner.setImage(request.getImageUrl());
        banner.setIsActive(request.getIsActive());

        banner = bannerRepository.save(banner);
        return convertToDTO(banner);
    }

    @Transactional
    public void deleteBanner(Integer id) {
        logger.info("Deleting banner with id: {}", id);
        Banner banner = bannerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found or deleted with id: " + id));
        banner.setIsDeleted(true);
        bannerRepository.save(banner);
    }

    private BannerDTO convertToDTO(Banner banner) {
        BannerDTO dto = new BannerDTO();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageUrl(banner.getImage());
        dto.setIsActive(banner.getIsActive());
        dto.setIsDeleted(banner.getIsDeleted());
        dto.setCreatedAt(banner.getCreatedAt());
        dto.setUpdatedAt(banner.getUpdatedAt());
        return dto;
    }

    private void validateUrl(String url) {
        if (url != null && !url.matches("^(https?:\\/\\/)?[\\w\\-\\.]+\\.[a-zA-Z]{2,}(\\/.*)?$")) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }
}