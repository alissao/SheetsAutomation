package com.example.googledriveprocessor.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints(GraalRuntimeHints.Registrar.class)
public class GraalRuntimeHints {
    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Allow common text encodings and mime types resources
            hints.resources().registerPattern("**/*.properties");
            hints.resources().registerPattern("**/mime.types");
            hints.resources().registerPattern("**/tika-mimetypes.xml");
            hints.resources().registerPattern("**/org/apache/tika/**");

            // ServiceLoader usages in Apache Tika
            hints.resources().registerPattern("META-INF/services/*");
        }
    }
}
