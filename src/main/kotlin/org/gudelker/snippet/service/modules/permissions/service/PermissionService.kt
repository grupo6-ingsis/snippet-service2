package org.gudelker.snippet.service.modules.permissions.service

import org.gudelker.snippet.service.modules.permissions.Permission
import org.gudelker.snippet.service.modules.permissions.repository.PermissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class PermissionService(private val permissionRepository: PermissionRepository) {

    @Transactional(readOnly = true)
    fun getAllPermissions(): List<Permission> {
        return permissionRepository.findAll()
    }
}
