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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.luke.sirius.MRServer.Utils.MRUtils;
import com.luke.sirius.MRServer.data.GitlabWebhookData;
import com.luke.sirius.MRServer.database.MergeRequestEntity;
import com.luke.sirius.MRServer.database.MergeRequestRepository;
import com.luke.sirius.utils.DateHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.Map;

@RestController
public class MRServer {

    @Autowired
    private MergeRequestRepository mergeRequestRepository;

    @PostMapping("merge-request/post")
    public void handlePost(HttpServletRequest request, @RequestBody GitlabWebhookData body, @RequestHeader HttpHeaders headers) {
        System.out.println("✦ " + DateHelper.currentDateTime() + " 收到新的 merge request webhook 事件: ");
        System.out.println("✦ request IP Address: " + request.getRemoteAddr());
        System.out.println("✦ request header: " + headers);

        try {
            if (body.object_attributes.state == GitlabWebhookData.ObjectAttributes.State.MERGED) {
                // 处理 merged 通知
                sentMessage(body);
            } else if (body.object_attributes.state == GitlabWebhookData.ObjectAttributes.State.OPENED) {
                // 处理 opened 通知
                saveOpenedMergeRequest(body);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("✦ 本次处理结束\n");
    }

    private static void sentMessage(GitlabWebhookData webhookData) throws JsonProcessingException {
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            System.out.println("✦ 当前事件为合并事件，原始数据:\n" + writer.writeValueAsString(webhookData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

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

        System.out.println("✦ 数据符合要求，准备发送飞书机器人消息");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> messageMap = MRUtils.getMergedBotMessage(webhookData, feishuOpenID);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(messageMap, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(feishuBotWebhookUrl, request, Map.class);
        System.out.println("✦ 飞书机器人消息发送完成，状态码: " + response.getStatusCode());
    }

    private void saveOpenedMergeRequest(GitlabWebhookData webhookData) {
        try {
            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String originalData = writer.writeValueAsString(webhookData);
            MergeRequestEntity entity = mergeRequestRepository.findById(webhookData.object_attributes.iid).orElse(new MergeRequestEntity(webhookData.object_attributes.iid, webhookData.object_attributes.url, originalData));
            final MergeRequestEntity updatedEmployee = mergeRequestRepository.save(entity);
            System.out.println("✦ 当前事件为创建事件，原始数据:\n" + originalData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

