package com.increff.invoice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Custom MongoDB converters for ZonedDateTime
 * MongoDB stores dates as java.util.Date, so we need converters to/from
 * ZonedDateTime
 */
public class MongoConverters {

    /**
     * Converts Date (from MongoDB) to ZonedDateTime
     */
    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(@NonNull Date source) {
            return source.toInstant().atZone(ZoneId.systemDefault());
        }
    }

    /**
     * Converts ZonedDateTime to Date (for MongoDB storage)
     */
    @WritingConverter
    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(@NonNull ZonedDateTime source) {
            return Date.from(source.toInstant());
        }
    }
}
