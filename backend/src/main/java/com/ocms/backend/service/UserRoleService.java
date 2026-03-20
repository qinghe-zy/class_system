package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.SysRoleMapper;
import com.ocms.backend.mapper.SysUserRoleMapper;
import com.ocms.backend.model.entity.SysRole;
import com.ocms.backend.model.entity.SysUserRole;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public UserRoleService(SysUserRoleMapper sysUserRoleMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    public String getRoleCodeByUserId(Long userId) {
        SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .last("limit 1"));
        if (userRole == null) {
            throw new BizException(500, "用户角色未配置");
        }
        SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
        if (role == null) {
            throw new BizException(500, "角色信息不存在");
        }
        return role.getRoleCode();
    }

    public Long getRoleIdByCode(String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            throw new BizException("角色不存在: " + roleCode);
        }
        return role.getId();
    }

    public void bindRole(Long userId, String roleCode) {
        Long roleId = getRoleIdByCode(roleCode);
        SysUserRole userRole = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRole == null) {
            userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
        } else {
            userRole.setRoleId(roleId);
            sysUserRoleMapper.updateById(userRole);
        }
    }
}
