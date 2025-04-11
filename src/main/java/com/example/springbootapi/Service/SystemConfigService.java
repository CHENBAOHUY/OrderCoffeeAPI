package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.SystemConfig;
import com.example.springbootapi.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    public List<SystemConfig> getAllSystemConfigs() {
        return systemConfigRepository.findAll();
    }

    public Optional<SystemConfig> getSystemConfigById(Integer id) {
        return systemConfigRepository.findById(id);
    }

    public SystemConfig addSystemConfig(SystemConfig systemConfig) {
        return systemConfigRepository.save(systemConfig);
    }

    public void deleteSystemConfig(Integer id) {
        systemConfigRepository.deleteById(id);
    }

    public SystemConfig getActiveConfig(String currencyCode) {
        return systemConfigRepository.findActiveConfigByCurrencyCode(currencyCode, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình cho " + currencyCode));
    }
}