package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocms.backend.common.BizException;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.ProfileUpdateRequest;
import com.ocms.backend.model.dto.UserUpsertRequest;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import com.ocms.backend.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;

    public UserService(SysUserMapper sysUserMapper, UserRoleService userRoleService) {
        this.sysUserMapper = sysUserMapper;
        this.userRoleService = userRoleService;
    }

    public PageResult<Map<String, Object>> pageUsers(Integer pageNum, Integer pageSize, String keyword, String role) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        wrapper.orderByDesc(SysUser::getId);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = result.getRecords().stream().map(user -> {
            String roleCode = userRoleService.getRoleCodeByUserId(user.getId());
            Map<String, Object> item = toMap(user, roleCode);
            return item;
        }).filter(item -> !StringUtils.hasText(role) || role.equals(item.get("role"))).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), records);
    }

    public Long createUser(UserUpsertRequest request) {
        SysUser exists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (exists != null) {
            throw new BizException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.hash(StringUtils.hasText(request.getPassword()) ? request.getPassword() : "123456"));
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartmentOrClass(request.getDepartmentOrClass());
        user.setStatus(request.getStatus());
        sysUserMapper.insert(user);
        userRoleService.bindRole(user.getId(), request.getRole());
        return user.getId();
    }

    public void updateUser(Long id, UserUpsertRequest request) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartmentOrClass(request.getDepartmentOrClass());
        user.setStatus(request.getStatus());
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(PasswordUtil.hash(request.getPassword()));
        }
        sysUserMapper.updateById(user);
        userRoleService.bindRole(user.getId(), request.getRole());
    }

    public void deleteUser(Long id) {
        sysUserMapper.deleteById(id);
    }

    public void resetPassword(Long id, String newPassword) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPassword(PasswordUtil.hash(StringUtils.hasText(newPassword) ? newPassword : "123456"));
        sysUserMapper.updateById(user);
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    public void updateProfile(ProfileUpdateRequest request) {
        Long userId = AuthContext.userId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartmentOrClass(request.getDepartmentOrClass());
        sysUserMapper.updateById(user);
    }

    private Map<String, Object> toMap(SysUser user, String roleCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("gender", user.getGender());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("departmentOrClass", user.getDepartmentOrClass());
        map.put("avatar", user.getAvatar());
        map.put("status", user.getStatus());
        map.put("role", roleCode);
        map.put("createdTime", user.getCreatedTime());
        map.put("updatedTime", user.getUpdatedTime());
        return map;
    }
}
