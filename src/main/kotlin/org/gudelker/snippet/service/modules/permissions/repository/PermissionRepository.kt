package org.gudelker.snippet.service.modules.permissions.repository

import org.gudelker.snippet.service.modules.permissions.Permission
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByName(name: String): Permission?
    fun findAllByNameIn(names: Collection<String>): List<Permission>
}