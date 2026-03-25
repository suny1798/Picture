package com.suny.picture.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.suny.picture.api.imagesearch.model.ImageSearchResult;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     *
     * @param url
     * @return
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            // 发起GET请求
            HttpResponse response = HttpUtil.createGet(url).execute();

            // 获取响应内容
            int statusCode = response.getStatus();
            String body = response.body();

            // 处理响应
            if (statusCode == 200) {
                // 解析 JSON 数据并处理
                return processResponse(body);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    /**
     * 处理接口响应内容
     *
     * @param responseBody 接口返回的JSON字符串
     */
    private static List<ImageSearchResult> processResponse(String responseBody) {

        // ===== 1️⃣ 先判断是不是 HTML =====
        if (responseBody != null && responseBody.contains("window.cardData")) {
            // 👉 说明是百度识图 HTML 页面

            // 1.1 提取 window.cardData
            Pattern pattern = Pattern.compile(
                    "window\\.cardData\\s*=\\s*(\\[.*?\\])\\s*;",
                    Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(responseBody);

            if (!matcher.find()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未解析到 cardData");
            }

            String jsonStr = matcher.group(1);

            // 1.2 解析 JSON
            JSONArray cardArray = JSONUtil.parseArray(jsonStr);

            List<ImageSearchResult> resultList = new ArrayList<>();

            for (Object obj : cardArray) {
                JSONObject card = (JSONObject) obj;

                // 👉 找 "same"
                if ("same".equals(card.getStr("cardName"))) {
                    JSONArray list = card
                            .getJSONObject("tplData")
                            .getJSONArray("list");

                    for (Object itemObj : list) {
                        JSONObject item = (JSONObject) itemObj;

                        ImageSearchResult result = new ImageSearchResult();
                        result.setThumbUrl(item.getStr("image_src"));
                        result.setFromUrl(item.getStr("url"));
                        resultList.add(result);
                    }
                }
            }

            return resultList;
        }

        // ===== 2️⃣ 原来的 JSON 逻辑（保留） =====
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(responseBody);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "返回结果不是合法JSON");
        }

        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }

        JSONObject data = jsonObject.getJSONObject("data");

        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }

        JSONArray list = data.getJSONArray("list");

        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=7687533025094688182&sign=1212162e63c3c0ab4ab2d01774425808&tpl_from=pc";
        List<ImageSearchResult> imageList = getImageList(url);
        System.out.println("搜索成功" + imageList);
    }
}
