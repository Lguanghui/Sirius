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

package com.luke.sirius.MRServer.data;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 使用 createMR 脚本创建 merge request 的时候，发送的数据
 */
@Data
public class MergeRequestCreateData {
    /**
     * 创建 merge request 时 at 的飞书用户 id
     */
    public Set<String> bot_message_at_ids;

    /**
     * merge request 链接
     */
    public String url;

    /**
     * merge request 的 iid
     */
    public long iid;

    /**
     * 自己的飞书 id
     */
    public String personal_openid;

    /**
     * 飞书机器人 webhook 地址
     */
    public String bot_webhook_url;

    /**
     * 创建人 git author
     */
    public String author;
}
