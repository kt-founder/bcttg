package com.bcttg.module.home.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class UpdateHomeModulesRequest {
    @NotEmpty
    private List<@Valid UpdateHomeModuleItemRequest> modules;

    public List<UpdateHomeModuleItemRequest> getModules() {
        return modules;
    }

    public void setModules(List<UpdateHomeModuleItemRequest> modules) {
        this.modules = modules;
    }
}
