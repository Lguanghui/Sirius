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

package com.luke.sirius.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    public static String currentDateTime() {
        return currentDateTime("yyyy-MM-dd HH:mm:ss");
    }

    public static  String currentDateTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.applyPattern(format);
        Date date = new Date();
        return dateFormat.format(date);
    }
}
