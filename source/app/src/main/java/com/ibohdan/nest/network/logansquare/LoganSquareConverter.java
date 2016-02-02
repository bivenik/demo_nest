package com.ibohdan.nest.network.logansquare;

import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import retrofit.mime.TypedString;

public class LoganSquareConverter implements Converter {

    @Nullable
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return LoganSquare.parse(body.in(), (Class) type);
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        try {
            return new TypedString(LoganSquare.serialize(object));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
