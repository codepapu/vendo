package com.vendo.analytics.dto.response;

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
public class UserAnalytics {
    private Long totalUsers;
    private Long newUsersToday;
    private Long activeUsers;
    private List<Map<String, Object>> userGrowth;
}
