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
import org.hibernate.annotations.Type;

@Entity
@Table(name = "merge_request")
public class MergeRequestEntity {

    @Id
    private Long id;
    private String url;

    /**
     * Lob 和 Column 的注解必须放在声明这里，不能放在 Getter 或 Setter 上
     */

    @Getter
    @Setter
    @Lob
    @Column(columnDefinition="TEXT")
    private String created_json_data_from_webhook;

    public MergeRequestEntity() {

    }

    public MergeRequestEntity(Long id, String url, String created_json_data_from_webhook) {
        this.id = id;
        this.url = url;
        this.created_json_data_from_webhook = created_json_data_from_webhook;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    @Column(name = "url", nullable = false)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
