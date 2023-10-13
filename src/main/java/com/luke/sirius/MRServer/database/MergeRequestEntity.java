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

package com.luke.sirius.MRServer.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merge_request")
public class MergeRequestEntity {

    @Id
    private Long id;
    /**
     * merge request 链接
     */
    @Getter
    @Setter
    private String url;

    /**
     * merge request 创建者的飞书 id
     */
    @Setter
    @Getter
    private String personal_openid;

    /**
     * merge request 创建者使用的机器人 webhook 地址
     */
    @Setter
    @Getter
    private String bot_webhook_url;

    /**
     * 创建 merge request 的时候 at 的用户。这个字段会存储在另一个 hash 表中
     */
    @Setter
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    @Column
    private Set<String> bot_message_at_ids = new HashSet<String>();

    /**
     * Lob 和 Column 的注解必须放在声明这里，不能放在 Getter 或 Setter 上
     */
    @Getter
    @Setter
    @Lob
    @Column(columnDefinition="TEXT")
    private String json_data_from_gitlab_webhook;

    /**
     * 作者
     */
    @Setter
    @Getter
    private String author;

    public MergeRequestEntity() {

    }

    /**
     * 构造器
     * @param id id
     * @param url url
     * @param created_json_data_from_webhook 来自 gitlab webhook 的数据
     * @param personal_openid 自己的飞书 id
     * @param bot_webhook_url 机器人 webhook 地址
     * @param bot_message_at_ids 想要 at 的 id
     */
    public MergeRequestEntity(Long id, String url, String created_json_data_from_webhook, String personal_openid, String bot_webhook_url, Set<String> bot_message_at_ids) {
        this.id = id;
        this.url = url;
        this.json_data_from_gitlab_webhook = created_json_data_from_webhook;
        this.personal_openid = personal_openid;
        this.bot_webhook_url = bot_webhook_url;
        this.bot_message_at_ids = bot_message_at_ids;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

}
