/*
 * Copyright 2016 Fritz Elfert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.whatsapp;

import java.io.File;
import java.io.Serializable;

/**
 * Media data representation of objects, serialized the field <b>thumb_image</b>
 * of the messages table in a WhatsApp msgstore backup.
 *
 * This class was hand-crafted to match the serialized object stream in said
 * database.
 */
public class MediaData implements Serializable {
    static final long serialVersionUID = -3211751283609594L;

    boolean downloadRetryEnabled;
    int faceX;
    int faceY;
    int failErrorCode;
    long fileSize;
    long progress;
    int suspiciousContent;
    boolean transcoded;
    boolean transferred;
    long trimFrom;
    long trimTo;
    byte[] cipherKey;
    File file;
    byte[] hmacKey;
    byte[] iv;
    byte[] mediaKey;
    byte[] refKey;
    String uploadUrl;

    @Override
    public String toString() {
        return "MediaData{" +
                "file='" + file +
                "', sz=" + fileSize +
                "', fX=" + faceX +
                "', fY=" + faceY +
                ", retE=" + downloadRetryEnabled +
                ", fEC=" + failErrorCode +
                ", susC=" + suspiciousContent +
                ", trF=" + trimFrom +
                ", trT=" + trimTo +
                ", tcd=" + transcoded +
                ", txd=" + transferred +
                ", progr=" + progress + "%" +
                ", ck=" + (null == cipherKey ? "null" : cipherKey.length) +
                ", hk=" + (null == hmacKey ? "null" : hmacKey.length) +
                ", iv=" + (null == iv ? "null" : iv.length) +
                ", mk=" + (null == mediaKey ? "null" : mediaKey.length) +
                ", rk=" + (null == refKey ? "null" : refKey.length) +
                ", uu=" + (null == uploadUrl ? "null" : "'" + uploadUrl + "'") +
                '}';
    }

    public int getFaceX() {
        return faceX;
    }

    public int getFaceY() {
        return faceY;
    }

    public File getFile() {
        return file;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isTransferred() {
        return transferred;
    }

    public boolean isTranscoded() {
        return transcoded;
    }

    public long getProgress() {
        return progress;
    }
}
