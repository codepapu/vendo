package com.nexus.common.config;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for soft delete functionality
 * Entities can use @FilterDef and @Filter annotations to enable soft delete
 */
@Configuration
public class SoftDeleteConfig {
    
    /**
     * Example usage in entity:
     * 
     * @FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
     * @Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
     * public class YourEntity extends BaseEntity {
     *     ...
     * }
     * 
     * Then in repository:
     * @Query("SELECT e FROM YourEntity e WHERE e.deletedAt IS NULL")
     * List<YourEntity> findAllActive();
     */
}
