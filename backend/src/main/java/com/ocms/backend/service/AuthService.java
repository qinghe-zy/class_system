package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.LoginRequest;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import com.ocms.backend.security.JwtService;
import com.ocms.backend.security.LoginUser;
import org.springframework.stereotype.Service;
import com.ocms.backend.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;
    private final JwtService jwtService;

    public AuthService(SysUserMapper sysUserMapper,
                       UserRoleService userRoleService,
                       JwtService jwtService) {
        this.sysUserMapper = sysUserMapper;
        this.userRoleService = userRoleService;
        this.jwtService = jwtService;
    }

    public Map<String, Object> login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "账号已被禁用");
        }
        String roleCode = userRoleService.getRoleCodeByUserId(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), roleCode);
        String token = jwtService.generateToken(loginUser);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", roleCode);
        result.put("userId", user.getId());
        result.put("realName", user.getRealName());
        return result;
    }

    public Map<String, Object> me() {
        Long userId = AuthContext.userId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        String roleCode = userRoleService.getRoleCodeByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("gender", user.getGender());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("departmentOrClass", user.getDepartmentOrClass());
        result.put("avatar", user.getAvatar());
        result.put("status", user.getStatus());
        result.put("role", roleCode);
        return result;
    }
}
