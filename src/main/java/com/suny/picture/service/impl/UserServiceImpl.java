package com.suny.picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suny.picture.constant.UserConstant;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.manager.auth.StpKit;
import com.suny.picture.mapper.UserMapper;
import com.suny.picture.model.dto.user.UserLoginRequest;
import com.suny.picture.model.dto.user.UserQueryRequest;
import com.suny.picture.model.dto.user.UserRegisterRequest;
import com.suny.picture.model.dto.user.UserVipRequest;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.entity.VipCode;
import com.suny.picture.model.enums.UserRoleEnum;
import com.suny.picture.model.vo.LoginUserVO;
import com.suny.picture.model.vo.UserVO;
import com.suny.picture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sun
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2026-03-18 10:31:59
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        //1.校验参数
        if (ObjUtil.isEmpty(userRegisterRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userRegisterRequest.getUserName().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不能为空");
        }
        if (userRegisterRequest.getUserName().length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不能超过10位");
        }
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userRegisterRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        //2.检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号重复");
        }
        //3.密码加密
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());
        //4.插入数据库
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserName(userRegisterRequest.getUserName() == null || userRegisterRequest.getUserName().isEmpty() ? "userName" : userRegisterRequest.getUserName());
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败,数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //校验
        if (ObjUtil.isEmpty(userLoginRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userLoginRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userLoginRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //密码加密
        String password = getEncryptPassword(userLoginRequest.getUserPassword());
        //查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userLoginRequest.getUserAccount());
        queryWrapper.eq("userPassword", password);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("userLogin fail, userAccount cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        //Sa-Token登录
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        //返回脱敏信息
        LoginUserVO userVO = getSafeUser(user);
        return userVO;
    }

    /**
     * 获取加密后的密码
     *
     * @param password
     * @return
     */
    @Override
    public String getEncryptPassword(String password) {
        //混淆 加盐密码
        return DigestUtils.md5DigestAsHex((password + "suny").getBytes());
    }

    /**
     * 获取脱敏用户信息
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getSafeUser(User user) {
        LoginUserVO userVO = new LoginUserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (ObjUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public Boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //从session中获取用户信息
        User userObj = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //以下操作会有额外的IO数据库访问操作
        if (userObj == null || userObj.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //查询数据库   ---防止信息更新
        long userId = userObj.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 根据用户查询请求条件构建QueryWrapper
     * 这是一个重写的方法，用于创建针对User实体的查询条件包装器
     *
     * @param userQueryRequest 用户查询请求对象，包含查询条件
     * @return 返回一个QueryWrapper<User>对象，用于构建数据库查询条件
     * 当前实现返回null，实际使用时应根据查询条件构建并返回相应的QueryWrapper
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userRole), "userRole", userRole);
        queryWrapper.like(ObjUtil.isNotEmpty(userName), "userName", userName);
        queryWrapper.like(ObjUtil.isNotEmpty(userAccount), "userAccount", userAccount);
        queryWrapper.like(ObjUtil.isNotEmpty(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    @Override
    public User editUser(HttpServletRequest request) {
        User loginUser = getLoginUser(request);

        return null;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean isVIP(User user) {
        return user != null && (UserRoleEnum.SVIP.getValue().equals(user.getUserRole()) || UserRoleEnum.FVIP.getValue().equals(user.getUserRole()));
    }

    /**
     * 用户兑换vip
     *
     * @param userVipRequest
     * @return
     */
    @Override
    public boolean userVip(UserVipRequest userVipRequest) {
        User user = this.getById(userVipRequest.getId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String vipCodeInput = userVipRequest.getVipCode();
        try {
            // 1. 读取兑换码
            List<VipCode> codeList = readVipCodes();
            VipCode targetCode = null;
            for (VipCode code : codeList) {
                if (code.getCode().equals(vipCodeInput)) {
                    targetCode = code;
                    break;
                }
            }
            // 2. 校验兑换码
            ThrowUtils.throwIf(targetCode == null, ErrorCode.PARAMS_ERROR, "兑换码不存在");
            ThrowUtils.throwIf(targetCode.getUsed(), ErrorCode.PARAMS_ERROR, "兑换码已使用");
            // 3. 计算过期时间
            Calendar calendar = Calendar.getInstance();
            if (targetCode.getMonths() >= 999) {
                // 永久会员
                calendar.set(2099, Calendar.DECEMBER, 31, 23, 59, 59);
            } else {
                calendar.add(Calendar.MONTH, targetCode.getMonths());
            }
            Date vipExpireTime = calendar.getTime();
            // 4. 更新兑换码状态
            targetCode.setUsed(true);
            writeVipCodes(codeList);
            // 5. 设置用户信息
            user.setVipCode(vipCodeInput);
            user.setVipExpireTime(vipExpireTime);
            // 会员编号（优化：短一点）
            Long vipNumber = IdUtil.getSnowflake(1, 1).nextId() % 100000000;
            user.setVipNumber(vipNumber);
            // 根据时长设置角色
            String userRole = targetCode.getMonths() >= 999 ? UserConstant.SUPER_ROLE : UserConstant.F_ROLE;
            user.setUserRole(userRole);
            boolean save = this.updateById(user);
            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "用户兑换vip失败");
            return true;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "兑换码系统异常");
        }
    }

    private static final String FILE_PATH = "src/main/resources/vipCode/vip-codes.json";

    private List<VipCode> readVipCodes() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(FILE_PATH), new TypeReference<List<VipCode>>() {
        });
    }

    private void writeVipCodes(List<VipCode> list) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), list);
    }

}




