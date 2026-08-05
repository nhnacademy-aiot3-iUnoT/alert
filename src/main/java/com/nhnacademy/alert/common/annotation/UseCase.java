package com.nhnacademy.alert.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface UseCase {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
