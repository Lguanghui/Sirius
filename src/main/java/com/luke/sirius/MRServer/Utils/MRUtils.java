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

package com.luke.sirius.MRServer.Utils;

import com.luke.sirius.MRServer.data.GitlabWebhookData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class MRUtils {
    public static Map<String, Object> getMergedBotMessage(GitlabWebhookData data, String openid) throws JsonProcessingException {
        String at = "";
        if (!openid.isEmpty()) {
            at = String.format("<at id=%s></at>", openid);
        }
        String jsonString = """
                {
                    "msg_type": "interactive",
                    "card":
                    {
                        "config":
                        {
                            "wide_screen_mode": true
                        },
                        "header":
                        {
                            "title":
                            {
                                "tag": "plain_text",
                                "content": "✅ merge request 已合并通知"
                            },
                            "template": "indigo"
                        },
                        "elements":
                        [
                            {
                                "tag": "div",
                                "text":
                                {
                                    "tag": "lark_md",
                                    "content": "{at} 您创建的 merge request 已被 {merged_by_user} 合并 🎉🎉🎉"
                                }
                            },
                            {
                                "tag": "div",
                                "fields":
                                [
                                    {
                                        "is_short": true,
                                        "text":
                                        {
                                            "tag": "lark_md",
                                            "content": "**🛖 仓库：**\\n{repo_name}"
                                        }
                                    },
                                    {
                                        "is_short": true,
                                        "text":
                                        {
                                            "tag": "lark_md",
                                            "content": "**✏️ 提交信息：**\\n{merge_request_title}"
                                        }
                                    },
                                    {
                                        "is_short": false,
                                        "text":
                                        {
                                            "tag": "lark_md",
                                            "content": ""
                                        }
                                    },
                                    {
                                        "is_short": true,
                                        "text":
                                        {
                                            "tag": "lark_md",
                                            "content": "**⏰ 创建时间：**\\n{created_time}"
                                        }
                                    },
                                    {
                                        "is_short": true,
                                        "text":
                                        {
                                            "tag": "lark_md",
                                            "content": "**⌛️ 更新时间：**\\n{update_time}"
                                        }
                                    }
                                ]
                            },
                            {
                                "tag": "action",
                                "actions":
                                [
                                    {
                                        "tag": "button",
                                        "text":
                                        {
                                            "tag": "plain_text",
                                            "content": "点我查看 merge request"
                                        },
                                        "type": "primary",
                                        "multi_url":
                                        {
                                            "url": "{merge_request_url}"
                                        }
                                    }
                                ]
                            },
                            {
                                "tag": "note",
                                "elements":
                                [
                                    {
                                        "tag": "plain_text",
                                        "content": "📟 Powered by KEPShellScripts"
                                    }
                                ]
                            }
                        ]
                    }
                }
                """
                .replace("{repo_name}", data.project.name)
                .replace("{merge_request_url}", data.object_attributes.url)
                .replace("{merge_request_title}", data.object_attributes.title)
                .replace("{update_time}", formatTime(data.object_attributes.updated_at))
                .replace("{created_time}", formatTime(data.object_attributes.created_at))
                .replace("{at}", at)
                .replace("{merged_by_user}", data.user.name)
                ;

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将带时区的日期字符串转为不带时区的
     * @param original 原始日期字符串
     * @return 转换结果
     */
    private static String formatTime(String original) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        Date date = null;
        try {
            date = sdf.parse(original);
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            newFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return newFormat.format(date);
        } catch (ParseException e) {
            return original;
        }
    }
}
