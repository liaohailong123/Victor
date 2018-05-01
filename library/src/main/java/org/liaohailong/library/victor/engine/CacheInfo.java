package org.liaohailong.library.victor.engine;

import java.util.Collections;
import java.util.Map;

/**
 * Describe as: 缓存信息
 * Created by LiaoHaiLong on 2018/5/1.
 */

public class CacheInfo {

    public String cookie;

    /**
     * ETag for cache coherency.
     */
    public String etag;

    /**
     * Date of this response as reported by the server.
     */
    public long serverDate;

    /**
     * The last modified date for the requested object.
     */
    public long lastModified;

    /**
     * TTL for this record.
     */
    public long ttl;

    /**
     * Soft TTL for this record.
     */
    public long softTtl;

    /**
     * Immutable response headers as received from server; must be non-null.
     */
    public Map<String, String> responseHeaders = Collections.emptyMap();

    /**
     * True if the entry is expired.
     */
    boolean isExpired() {
        return this.ttl < System.currentTimeMillis();
    }

    /**
     * True if a refresh is needed from the original data source.
     */
    boolean refreshNeeded() {
        return this.softTtl < System.currentTimeMillis();
    }
}
