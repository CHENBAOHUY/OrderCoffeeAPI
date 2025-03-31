package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.SystemConfig;
import com.example.springbootapi.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;

    @Autowired
    public SystemConfigService(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public List<SystemConfig> getAllSystemConfigs() {
        return systemConfigRepository.findAll();
    }

    public Optional<SystemConfig> getSystemConfigById(Integer id) {
        return systemConfigRepository.findById(id);
    }

    public SystemConfig createConfig(SystemConfig config) {
        return systemConfigRepository.save(config);
    }

    public void deleteSystemConfig(Integer id) {
        systemConfigRepository.deleteById(id);
    }

    public SystemConfig addSystemConfig(SystemConfig systemConfig) {
        return systemConfigRepository.save(systemConfig);
    }
}
