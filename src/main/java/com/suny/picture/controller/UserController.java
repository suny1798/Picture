package com.suny.picture.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suny.picture.annotation.AuthCheck;
import com.suny.picture.common.BaseResponse;
import com.suny.picture.common.DeleteRequest;
import com.suny.picture.common.PageRequest;
import com.suny.picture.common.ResultUtils;
import com.suny.picture.config.CosClientConfig;
import com.suny.picture.constant.UserConstant;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.manager.CosManager;
import com.suny.picture.manager.upload.FilePictureUpload;
import com.suny.picture.manager.upload.PictureUploadTemplate;
import com.suny.picture.model.dto.file.UploadPictureResult;
import com.suny.picture.model.dto.user.*;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.LoginUserVO;
import com.suny.picture.model.vo.UserVO;
import com.suny.picture.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private FilePictureUpload filePictureUpload;

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long userId = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (attribute != null) {
            throw new BusinessException(ErrorCode.LOGINED_ERROR);
        }
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO userLogin = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(userLogin);
    }

    /**
     * 用户编辑信息
     */
    @ApiOperation(value = "用户编辑信息")
    @PostMapping("/edit")
    public BaseResponse<Boolean> userEditInfo(@RequestBody UserEditRequest userEditRequest) {
        if (userEditRequest == null || userEditRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String password = userService.getEncryptPassword(userEditRequest.getUserPassword());
        User user = new User();
        BeanUtils.copyProperties(userEditRequest, user);
        user.setUserPassword(password);
        boolean save = userService.updateById(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户上传头像
     */
    @ApiOperation(value = "用户上传头像")
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadUserAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        // 1. 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 2. 生成文件路径
        String filePrefix = String.format(
                "public/avatar/%d",
                loginUser.getId()
        );
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        // 3. 上传到 COS
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(file, filePrefix);
        // 4. 返回图片路径
        String thumbnailUrl = uploadPictureResult.getThumbnailUrl();
        ThrowUtils.throwIf(thumbnailUrl == null, ErrorCode.OPERATION_ERROR);
        // 5. 更新用户数据库信息
        loginUser.setUserAvatar(thumbnailUrl);
        userService.updateById(loginUser);
        return ResultUtils.success(thumbnailUrl);
    }

    /**
     * 获取当前登录用户
     */
    @ApiOperation(value = "获取当前登录用户")
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getSafeUser(loginUser));
    }

    /**
     * 用户注销
     */
    @ApiOperation(value = "用户注销")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 创建用户
     */
    @ApiOperation(value = "管理员 创建用户")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String password = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(password);

        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户信息
     */
    @ApiOperation(value = "管理员 根据 id 获取用户信息")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取用户包装信息
     */
    @ApiOperation(value = "根据 id 获取用户脱敏信息")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户信息
     */
    @ApiOperation(value = "管理员 删除用户信息")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    /**
     * 更新用户
     */
    @ApiOperation(value = "管理员 更新用户")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean save = userService.updateById(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     *  分页获取用户信息
     * @param
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "管理员 分页获取用户信息")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        Page<User> page = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, page.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(page.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);

    }

}
