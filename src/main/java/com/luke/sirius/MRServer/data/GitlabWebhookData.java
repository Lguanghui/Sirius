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

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.List;

/**
 * Gitlab 收到 merge request 事件后发送的 post 请求的 body
 */
@Data
public class GitlabWebhookData {
    public String object_kind;
    public String event_type;
    public UserData user;
    public Project project;
    public ObjectAttributes object_attributes;
    public Repository repository;
    public List<Label> labels;

    @Data
    public static class UserData {
        public Integer id;
        public String name;
        public String username;
        public String avatar_url;
        public String email;
    }

    @Data
    public static class Project {
        public String id;
        public String name;
        public String description;
        public String web_url;
        public String avatar_url;
        public String git_ssh_url;
        public String git_http_url;
        public String namespace;
        public Integer visibility_level;
        public String path_with_namespace;
        public String default_branch;
        public String ci_config_path;
        public String homepage;
        public String url;
        public String ssh_url;
        public String http_url;
    }

    @Data
    public static class ObjectAttributes {
        public String assignee_id;
        public String author_id;
        public String created_at;
        public String description;
        public Boolean draft;
        public Integer head_pipeline_id;
        public Integer id;
        public Integer iid;
        public String merge_status;
        public String source_branch;
        public Integer source_project_id;
        public String target_branch;
        public String target_project_id;
        /**
         * MR 的标题
         */
        public String title;
        public String updated_at;
        public String updated_by_id;
        /**
         * MR 的链接
         */
        public String url;
        public LastCommit last_commit;
        /**
         * MR 状态。merged 表示已合并
         */
        public State state;

        public enum State {
            CLOSED("closed"),
            MERGED("merged"),
            OPENED("opened"),
            LOCKED("locked"),
            UNKNOWN("unknown");

            @JsonValue
            private final String state;
            State(String state) {
                this.state = state;
            }

            public String getValue() {
                return state;
            }
        }

        @Data
        public static class LastCommit {
            public String id;
            public String message;
            public String title;
            public String timestamp;
            public String url;
            public Author author;

            @Data
            public static class Author {
                public String name;
                public String email;
            }
        }
    }

    @Data
    public static class Repository {
        public String name;
        public String url;
        public String description;
        public String homepage;
    }

    @Data
    public static class Label {
        public String id;
        public String title;
        public String description;
    }
}