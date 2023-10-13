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
import com.luke.sirius.MRServer.data.MergeRequestCreateData;
import com.luke.sirius.MRServer.database.MergeRequestEntity;
import com.luke.sirius.MRServer.database.MergeRequestRepository;
import com.luke.sirius.utils.DateHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
public class MRServer {

    @Autowired
    private MergeRequestRepository mergeRequestRepository;

    /**
     * 终端使用 createMR 脚本创建 merge request 时调用的接口
     * @param request request
     */
    @PostMapping("merge-request/create")
    public void handleCreateMergeRequest(HttpServletRequest request, @RequestBody MergeRequestCreateData createData, @RequestHeader HttpHeaders headers) {
        MRUtils.printMessage("使用脚本创建了 merge request，iid: " + createData.iid);
        MergeRequestEntity entity = mergeRequestRepository.findById(createData.iid).orElse(new MergeRequestEntity(createData.iid,
                createData.url,
                null,
                createData.personal_openid,
                createData.bot_webhook_url,
                createData.bot_message_at_ids));
        entity.setAuthor(createData.author);

        mergeRequestRepository.save(entity);
        MRUtils.printMessage("新的 merge request 数据入库成功");
    }

    /**
     * 来自 Gitlab webhook 的请求。每次有 merge request 事件时都会请求
     * @param request request
     * @param body body
     * @param headers headers
     */
    @PostMapping("merge-request/post")
    public void handlePost(HttpServletRequest request, @RequestBody GitlabWebhookData body, @RequestHeader HttpHeaders headers) {
        System.out.println("✦ " + DateHelper.currentDateTime() + " 收到新的 merge request webhook 事件: ");
        System.out.println("✦ request IP Address: " + request.getRemoteAddr());
        System.out.println("✦ request header: " + headers);

        try {
            if (body.object_attributes.state == GitlabWebhookData.ObjectAttributes.State.OPENED) {
                // 处理 opened 通知
                saveOpenedMergeRequest(body);
            } else {
                if (body.object_attributes.state == GitlabWebhookData.ObjectAttributes.State.MERGED) {
                    // 处理 merged 通知
                    sentMessage(body);
                }
                // 这个 merge request 被合并或者被关闭，从数据库中移除
                removeMergeRequestEntity(body);
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
            String feishuBotWebhookUrl = "";
            String feishuOpenID = "";
            for (GitlabWebhookData.Label label : webhookData.labels) {
                if (label.title.contains("webhook-")) {
                    feishuBotWebhookUrl = label.description;
                } else if (label.title.contains("id-")) {
                    feishuOpenID = label.description;
                }
            }

            ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String originalData = writer.writeValueAsString(webhookData);
            MergeRequestEntity entity = mergeRequestRepository.findById(webhookData.object_attributes.iid).orElse(new MergeRequestEntity(webhookData.object_attributes.iid, webhookData.object_attributes.url, originalData, feishuOpenID, feishuBotWebhookUrl, null));
            entity.setJson_data_from_gitlab_webhook(originalData);
            final MergeRequestEntity updatedEmployee = mergeRequestRepository.save(entity);
            System.out.println("✦ 当前事件为创建事件，原始数据:\n" + originalData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * merge request 被合并后，从数据库中移除
     * @param webhookData 数据
     */
    private void removeMergeRequestEntity(GitlabWebhookData webhookData) {
        Optional<MergeRequestEntity> entity = mergeRequestRepository.findById(webhookData.object_attributes.iid);
        System.out.println("✦ 当前 merge request 状态为：" + webhookData.object_attributes.state + "，准备在数据库中将其移除");
        entity.ifPresent(mergeRequestEntity -> {
            System.out.println("✦ 数据库中 iid 为" + mergeRequestEntity.getId() + "的实体已删除");
            mergeRequestRepository.delete(mergeRequestEntity);
        });
    }

    /**
     * 定时任务，周一至周五 10 点 ～ 18:59 点 之间每隔 30 分钟执行一次
     * <a href="https://crontab.cronhub.io/">cron 表达式校验</a>
     */
    @Scheduled(cron = "0 0/30 10-18 ? * MON-FRI")
//    @Scheduled(fixedDelay = 10000L)
    public void job2() {
        System.out.println("✦ 定时任务开始执行");
        List<MergeRequestEntity> mergeRequestEntities = mergeRequestRepository.findAll();
        for (MergeRequestEntity entity : mergeRequestEntities) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                GitlabWebhookData webhookData = objectMapper.readValue(entity.getJson_data_from_gitlab_webhook(), GitlabWebhookData.class);
                Set<String> bot_message_at_ids = entity.getBot_message_at_ids();
                String author = entity.getAuthor();

                MRUtils.printMessage("数据库数据读取成功，准备发送提醒消息，iid: " + entity.getId());

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> messageMap = MRUtils.generateRemindBotMessage(webhookData, bot_message_at_ids, author);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(messageMap, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(entity.getBot_webhook_url(), request, Map.class);
                System.out.println("✦ 飞书机器人消息发送完成，状态码: " + response.getStatusCode());

            } catch (JsonProcessingException e) {
                System.out.println("✦ 将 json string 转为对象时失败");
            }
        }
    }
}

