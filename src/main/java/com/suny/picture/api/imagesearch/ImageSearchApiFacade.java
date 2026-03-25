package com.suny.picture.api.imagesearch;

import com.suny.picture.api.imagesearch.model.ImageSearchResult;
import com.suny.picture.api.imagesearch.sub.GetImageFirstUrlApi;
import com.suny.picture.api.imagesearch.sub.GetImageListApi;
import com.suny.picture.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imagePageUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://suny-1329292960.cos.ap-beijing.myqcloud.com/public/avatar/2034125975641972738/2026-03-24_BIQOXVpXTFt3du51.jpg";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
