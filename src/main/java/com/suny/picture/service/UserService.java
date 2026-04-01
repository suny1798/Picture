package com.suny.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suny.picture.model.dto.user.UserLoginRequest;
import com.suny.picture.model.dto.user.UserQueryRequest;
import com.suny.picture.model.dto.user.UserRegisterRequest;
import com.suny.picture.model.dto.user.UserVipRequest;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.LoginUserVO;
import com.suny.picture.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sun
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-03-18 10:31:59
 */

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 加密用户密码的方法
     *
     * @param password 原始密码字符串
     * @return 加密后的密码字符串
     */
    String getEncryptPassword(String password);

    /**
     * 获取脱敏用户登录信息
     *
     * @param user
     * @return
     */
    LoginUserVO getSafeUser(User user);

    /**
     * 获取脱敏用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏用户信息列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 根据用户查询请求参数构建查询条件包装器
     *
     * @param userQueryRequest 用户查询请求对象，包含查询条件
     * @return QueryWrapper<User> 返回一个包含查询条件的MyBatis-Plus的QueryWrapper对象，用于构建数据库查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户编辑信息
     * @param request
     * @return
     */
    User editUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 是否为超级会员
     * @param user
     * @return
     */
    boolean isVIP(User user);

    /**
     * 用户兑换会员
     *
     * @param userVipRequest
     * @return
     */
    boolean userVip(UserVipRequest userVipRequest);
}
