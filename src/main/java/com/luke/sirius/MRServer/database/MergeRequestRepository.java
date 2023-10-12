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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Long 对应 MergeReqeustEntity 里面的 id 类型
 * 这个接口定义了一系列操作数据库的方法
 */
@Repository
public interface MergeRequestRepository extends JpaRepository<MergeRequestEntity, Long> {

}
