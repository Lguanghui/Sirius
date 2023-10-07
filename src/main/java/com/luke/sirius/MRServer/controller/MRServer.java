/*
 * Copyright (c) 2023, Guanghui Liang. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luke.sirius.MRServer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.luke.sirius.MRServer.Utils.MRUtils;
import com.luke.sirius.MRServer.data.GitlabWebhookData;
import com.luke.sirius.utils.DateHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.Map;

@RestController
public class MRServer {

    @PostMapping("merge-request/post")
    public void handlePost(HttpServletRequest request, @RequestBody GitlabWebhookData body) {
        System.out.println(DateHelper.currentDateTime() + " 收到新的 merge request webhook事件: ");
        System.out.println("webhook 数据:\n" + body.toString());
        try {
            sentMessage(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sentMessage(GitlabWebhookData webhookData) throws JsonProcessingException {

        // 只处理 merged 通知
        if (webhookData.object_attributes.state != GitlabWebhookData.ObjectAttributes.State.MERGED) return;

        boolean hasFeishuBotWebhookUrl = false;
        String feishuBotWebhookUrl = "";
        String feishuOpenID = "";
        for (GitlabWebhookData.Label label : webhookData.labels) {
            if (label.title.contains("webhook-")) {
                hasFeishuBotWebhookUrl = true;
                feishuBotWebhookUrl = label.description;
            } else if (label.title.contains("id-")) {
                feishuOpenID = label.description;
            }
        }

        if (!hasFeishuBotWebhookUrl) return;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> messageMap = MRUtils.getMergedBotMessage(webhookData, feishuOpenID);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(messageMap, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(feishuBotWebhookUrl, request, Map.class);
        System.out.println("飞书机器人消息发送完成，状态码: " + response.getStatusCode());
    }
}

