package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.SystemConfig;
import com.example.springbootapi.Service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping
    public List<SystemConfig> getAllSystemConfigs() {
        return systemConfigService.getAllSystemConfigs();
    }

    @GetMapping("/{id}")
    public Optional<SystemConfig> getSystemConfigById(@PathVariable Integer id) {
        return systemConfigService.getSystemConfigById(id);
    }

    @PostMapping
    public SystemConfig addSystemConfig(@RequestBody SystemConfig systemConfig) {
        return systemConfigService.addSystemConfig(systemConfig);
    }

    @DeleteMapping("/{id}")
    public void deleteSystemConfig(@PathVariable Integer id) {
        systemConfigService.deleteSystemConfig(id);
    }
}
