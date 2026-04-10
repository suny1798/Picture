package com.suny.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suny.picture.model.dto.space.SpaceAddRequest;
import com.suny.picture.model.dto.space.SpaceQueryRequest;
import com.suny.picture.model.dto.space.analyze.*;
import com.suny.picture.model.entity.Space;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.SpaceVO;
import com.suny.picture.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceUserAnalyzeResponse;
import com.suny.picture.service.impl.SpaceSizeAnalyzeResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sun
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-03-23 18:28:40
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 空间使用情况分析
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 空间类别情况分析
     * @param spaceCategoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 空间标签情况分析
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 空间图片大小情况分析
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 空间用户行为分析
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 查看空间使用排行
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
