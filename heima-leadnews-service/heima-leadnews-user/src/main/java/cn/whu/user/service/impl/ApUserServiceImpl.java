package cn.whu.user.service.impl;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import cn.whu.model.user.dtos.LoginDto;
import cn.whu.model.user.pojos.ApUser;
import cn.whu.user.mapper.ApUserMapper;
import cn.whu.user.service.ApUserService;
import cn.whu.utils.common.AppJwtUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional // 事务
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    /**
     * app端登陆功能
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        // 1. 正常登录: 用户名 密码
        // 不null 不'' 也不' '
        if (StringUtils.isNotBlank(dto.getPassword()) && StringUtils.isNotBlank(dto.getPhone())) {
            // 1.1 根据手机号查询用户信息
            //ApUser user = getOne(new LambdaQueryWrapper<ApUser>().eq(ApUser::getPhone, dto.getPhone()));
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            // Wrappers.<ApUser>lambdaQuery() 就是 new LambdaQueryWrapper<ApUser>()  二者完全一样
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "该用户不存在");
            }
            // 1.2 比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!pswd.equals(dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            // 1.3 返回数据 jwt：就是token    user: 就是user   （二者为data的一个属性）
            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setSalt("");
            dbUser.setPassword("");//注意有些私密信息不能返回
            map.put("user", dbUser); // 直接返回，会自动序列化

            return ResponseResult.okResult(map); // map作为data返回即可
        }else {
            // 2. 游客登录
            // 没有用户，data中就不需要user,给个token就行了，用id=0L生成
            Map<String, Object> map = new HashMap<>();
            map.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }
}
