package com.maple.ai.job.hunting.service.ai.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.maple.ai.job.hunting.config.AppBizConfig;
import com.maple.ai.job.hunting.frame.exp.ApplicationException;
import com.maple.ai.job.hunting.model.Message;
import com.maple.ai.job.hunting.utils.FileTypeDetector;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * 月之暗面 AI工具类
 * 文件相关操作不消耗token；同时也没有rpm【每分钟请求数】限制
 *
 * @author maple
 * @since 2024/05/10
 */
@Service
@SuppressWarnings({"resource", "unused"})
public class MoonshotAiUtils {

    private static final String MODELS_URL = "https://api.moonshot.cn/v1/models";
    private static final String FILES_URL = "https://api.moonshot.cn/v1/files";
    private static final String ESTIMATE_TOKEN_COUNT_URL = "https://api.moonshot.cn/v1/tokenizers/estimate-token-count";
    private static final String QUERY_BALANCE_URL = "https://api.moonshot.cn/v1/users/me/balance";
    private static final String CHAT_COMPLETION_URL = "https://api.moonshot.cn/v1/chat/completions";

    @Resource
    private AppBizConfig appBizConfig;

    @Value("${spring.ai.kimi.api-key}")
    private String apiKey;

    /**
     * 获取型号列表
     *
     * @return <p><p/>
     * {
     * "object": "list",
     * "data": [
     * {
     * "created": 1715145123,
     * "id": "moonshot-v1-8k",
     * "object": "model",
     * "owned_by": "moonshot",
     * "permission": [
     * {
     * "created": 0,
     * "id": "",
     * "object": "",
     * "allow_create_engine": false,
     * "allow_sampling": false,
     * "allow_logprobs": false,
     * "allow_search_indices": false,
     * "allow_view": false,
     * "allow_fine_tuning": false,
     * "organization": "public",
     * "group": "public",
     * "is_blocking": false
     * }
     * ],
     * "root": "",
     * "parent": ""
     * },
     * {
     * "created": 1715145123,
     * "id": "moonshot-v1-32k",
     * "object": "model",
     * "owned_by": "moonshot",
     * "permission": [
     * {
     * "created": 0,
     * "id": "",
     * "object": "",
     * "allow_create_engine": false,
     * "allow_sampling": false,
     * "allow_logprobs": false,
     * "allow_search_indices": false,
     * "allow_view": false,
     * "allow_fine_tuning": false,
     * "organization": "public",
     * "group": "public",
     * "is_blocking": false
     * }
     * ],
     * "root": "",
     * "parent": ""
     * },
     * {
     * "created": 1715145123,
     * "id": "moonshot-v1-128k",
     * "object": "model",
     * "owned_by": "moonshot",
     * "permission": [
     * {
     * "created": 0,
     * "id": "",
     * "object": "",
     * "allow_create_engine": false,
     * "allow_sampling": false,
     * "allow_logprobs": false,
     * "allow_search_indices": false,
     * "allow_view": false,
     * "allow_fine_tuning": false,
     * "organization": "public",
     * "group": "public",
     * "is_blocking": false
     * }
     * ],
     * "root": "",
     * "parent": ""
     * }
     * ]
     * }
     */
    public JSONArray getModelList() {
        String body = getCommonRequest(MODELS_URL)
                .execute()
                .body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if (!jsonObject.containsKey("data")) {
            throw new ApplicationException("获取模型列表失败 msg:" + body);
        }
        return jsonObject.getJSONArray("data");
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return <p></p>
     * {
     * "id": "couea9kubmsaj67inrfg",
     * "object": "file",
     * "bytes": 31794,
     * "created_at": 1715266854,
     * "filename": "aaa.docx",
     * "purpose": "file-extract",
     * "status": "ok",
     * "status_details": ""
     * }
     */
    public JSONObject uploadFile(@NonNull File file) {
        JSONObject bodyJson = JSONUtil.parseObj(getCommonRequest(FILES_URL)
                .method(Method.POST)
                .header("purpose", "file-extract")
                .form("file", file)
                .execute()
                .body());
        if (bodyJson.containsKey("error")) {
            throw new ApplicationException("上传文件失败 msg:" + bodyJson.getStr("error"));
        }
        return bodyJson;
    }

    public JSONObject uploadFile(byte @NonNull [] fileBytes) {
        JSONObject bodyJson = JSONUtil.parseObj(getCommonRequest(FILES_URL)
                .method(Method.POST)
                .header("purpose", "file-extract")
                .form("file", fileBytes, "resume" + IdUtil.fastUUID() + "." + FileTypeDetector.detectFileType(fileBytes))
                .execute()
                .body());
        if (bodyJson.containsKey("error")) {
            throw new ApplicationException("上传文件失败 msg:" + bodyJson.getStr("error"));
        }
        return bodyJson;
    }

    /**
     * 获取文件列表
     *
     * @return <p></p>
     * {
     * "object": "list",
     * "data": [
     * {
     * "id": "coudggi2jko6b70kt0fg",
     * "object": "file",
     * "bytes": 204949,
     * "created_at": 1715263554,
     * "filename": "aaa.pdf",
     * "purpose": "file-extract",
     * "status": "ok",
     * "status_details": ""
     * },
     * {
     * "id": "coudh5gnsmmqhvj0lbrg",
     * "object": "file",
     * "bytes": 204949,
     * "created_at": 1715263638,
     * "filename": "bbb.pdf",
     * "purpose": "file-extract",
     * "status": "ok",
     * "status_details": ""
     * }
     * ]
     * }
     */
    public JSONArray getFileList() {
        String body = getCommonRequest(FILES_URL)
                .execute()
                .body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if (!jsonObject.containsKey("data")) {
            throw new ApplicationException("获取文件列表失败 msg:" + body);
        }
        return jsonObject.getJSONArray("data");
    }

    /**
     * 删除文件
     *
     * @param fileId 文件 ID
     * @return <p><p/>
     * {
     * "deleted": true,
     * "id": "coudggi2jko6b70kt0fg",
     * "object": "file"
     * }
     */
    public JSONObject deleteFile(@NonNull String fileId) {
        return JSONUtil.parseObj(getCommonRequest(FILES_URL + "/" + fileId)
                .method(Method.DELETE)
                .execute()
                .body());
    }

    /**
     * 获取文件详细信息
     *
     * @param fileId 文件 ID
     * @return <p></p>
     * {
     * "id": "couea9kubmsaj67inrfg",
     * "object": "file",
     * "bytes": 31794,
     * "created_at": 1715266854,
     * "filename": "林亮-文华学院-后端.docx",
     * "purpose": "file-extract",
     * "status": "ok",
     * "status_details": ""
     * }
     */
    public JSONObject getFileDetail(@NonNull String fileId) {
        return JSONUtil.parseObj(getCommonRequest(FILES_URL + "/" + fileId)
                .execute()
                .body());
    }

    /**
     * 获取文件内容
     *
     * @param fileId 文件 ID
     * @return <p></p>
     * <p>
     * {
     * "content": "aa（Java后端 3年多）\n\nTel: aa  e-mail: aa@qq.com\n\n教育经历\n\n学校 aa学院       \t\t专业 计算机科学与技术       \t\t\t学历  本科",
     * "file_type": "application/docx",
     * "filename": "aa-bb-后端.docx",
     * "title": "",
     * "type": "file"
     * }
     */
    public JSONObject getFileContent(@NonNull String fileId) {
        return JSONUtil.parseObj(getCommonRequest(FILES_URL + "/" + fileId + "/content")
                .execute()
                .body());
    }

    /**
     * 估计令牌计数
     *
     * @param model    模型：moonshot-v1-8k
     * @param messages List.of(new Message(RoleEnum.system.name, "你是java工程师"),new Message(RoleEnum.user.name, "你是应聘者")
     * @return <p></p>
     * {
     * "code": 0,
     * "data": {
     * "total_tokens": 16
     * },
     * "scode": "0x0",
     * "status": true
     * }
     */
    public JSONObject estimateTokenCount(@NonNull String model, @NonNull List<Message> messages) {
        String requestBody = new JSONObject()
                .putOpt("model", model)
                .putOpt("messages", messages)
                .toString();
        return JSONUtil.parseObj(getCommonRequest(ESTIMATE_TOKEN_COUNT_URL)
                .method(Method.POST)
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(requestBody)
                .execute()
                .body());
    }

    /**
     * 查询余额
     *
     * @return <p></p>
     * {
     * "code": 0,
     * "data": {
     * "available_balance": 14.99926,
     * "voucher_balance": 14.99926,
     * "cash_balance": 0
     * },
     * "scode": "0x0",
     * "status": true
     * }
     */
    public String queryBalance() {
        return getCommonRequest(QUERY_BALANCE_URL)
                .method(Method.GET)
                .execute()
                .body();
    }

    @SneakyThrows
    public void chat(@NonNull String model, @NonNull List<Message> messages) {
        String requestBody = new JSONObject()
                .putOpt("model", model)
                .putOpt("messages", messages)
                .putOpt("stream", true)
                .toString();
        Request okhttpRequest = new Request.Builder()
                .url(CHAT_COMPLETION_URL)
                .post(RequestBody.create(requestBody, MediaType.get(ContentType.JSON.getValue())))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
        Call call = new OkHttpClient().newCall(okhttpRequest);
        Response okhttpResponse = call.execute();
        BufferedReader reader = new BufferedReader(okhttpResponse.body().charStream());
        String line;
        while ((line = reader.readLine()) != null) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            if (JSONUtil.isTypeJSON(line)) {
                Optional.of(JSONUtil.parseObj(line))
                        .map(x -> x.getJSONObject("error"))
                        .map(x -> x.getStr("message"))
                        .ifPresent(x -> System.out.println("error: " + x));
                return;
            }
            line = StrUtil.replace(line, "data: ", StrUtil.EMPTY);
            if (StrUtil.equals("[DONE]", line) || !JSONUtil.isTypeJSON(line)) {
                return;
            }
            Optional.of(JSONUtil.parseObj(line))
                    .map(x -> x.getJSONArray("choices"))
                    .filter(CollUtil::isNotEmpty)
                    .map(x -> (JSONObject) x.get(0))
                    .map(x -> x.getJSONObject("delta"))
                    .map(x -> x.getStr("content"))
                    .ifPresent(x -> System.out.println("rowData: " + x));
        }
    }

    private HttpRequest getCommonRequest(@NonNull String url) {
        return HttpRequest.of(url).header(Header.AUTHORIZATION, "Bearer " + apiKey);
    }

}