package com.vendo.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    
    private DashboardStats stats;
    private List<Map<String, Object>> recentOrders;
    private List<Map<String, Object>> recentUsers;
    private List<Map<String, Object>> systemHealth;
}
