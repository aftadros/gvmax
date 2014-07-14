/*******************************************************************************
 * Copyright (c) 2013 Hani Naguib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Hani Naguib - initial API and implementation
 ******************************************************************************/
package com.gvmax.common.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * Utility class used to convert objects to / from Json.
 */
public final class JsonUtils {
    /** Date formater used to format dates */
    public static final String DATE_FORMAT = "MMM dd, yyyy hh:mm:ss a";
    /** Jackson mapper used under covers */
    private static ObjectMapper mapper = new ObjectMapper();
    /** Jackson mapper used under covers (pretty print)*/
    private static ObjectMapper prettyMapper = new ObjectMapper();

    /**
     * Utility hidding constructor.
     */
    private JsonUtils() {}

    static {
        // Init mappers
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        prettyMapper.setSerializationInclusion(Include.NON_NULL);
        prettyMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        prettyMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Converts an object into a json string.
     *
     * @param obj
     *            The object to convert to json. If null this method returns
     *            null.
     * @return Json string representation of object
     * @throws IOException
     *             Can throw IOExceptions
     */
    public static String toJson(Object obj) throws IOException {
        return toJson(obj, false);
    }

    public static String toJson(Object obj, boolean prettyPrint) throws IOException {
        if (obj == null) {
            return null;
        }
        if (prettyPrint) {
            return prettyMapper.writeValueAsString(obj);
        }
        return mapper.writeValueAsString(obj);
    }

    public static String format(String json, boolean pretty) {
        try {
            JsonNode node = fromJson(json, JsonNode.class);
            return toJson(node, pretty);
        } catch (Exception e) {
            return json;
        }
    }

    /**
     * Converts a json string into an object. If any of the parameters are null
     * this method returns null.
     *
     * @param json
     *            The json string.
     * @param valueType
     *            The required object type.
     * @return The object or null
     * @throws IOException
     *             Can throw IOExceptions
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws IOException {
        if (json == null || valueType == null) {
            return null;
        }
        try {
            return mapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new IOException("error converting json to '" + valueType.getCanonicalName() + "', json=" + json + "'", e);
        }
    }

    // -----------------
    // COLLECTIONS
    // -----------------

    public static <T> List<T> fromJsonList(String json, Class<T> elementClass) throws IOException {
        if (json == null || elementClass == null) {
            return null;
        }
        CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        return mapper.readValue(json, type);
    }

}
