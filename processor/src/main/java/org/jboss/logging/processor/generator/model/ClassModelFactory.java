/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.generator.model;

import static org.jboss.logging.processor.generator.model.ClassModelHelper.implementationClassName;
import static org.jboss.logging.processor.util.TranslationHelper.getEnclosingTranslationClassName;

import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;

import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * Creates a class model for the message interface.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClassModelFactory {

    /**
     * Private constructor for the factory.
     */
    private ClassModelFactory() {

    }

    /**
     * Creates an implementation code model from the message interface.
     *
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement
     *
     * @return the class model used to implement the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or {@link MessageLogger @MessageLogger}
     */
    public static ClassModel implementation(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface) throws IllegalArgumentException {
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            return new MessageBundleImplementor(processingEnv, messageInterface);
        }
        if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            return new MessageLoggerImplementor(processingEnv, messageInterface);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }

    /**
     * Creates a class model for created translation implementations of the message interface.
     * <p/>
     * <b>Note:</b> The implementation class must exist before the translation implementations can be created.
     *
     * @param processingEnv     the processing environment
     * @param messageInterface  the message interface to implement.
     * @param translationSuffix the translation locale suffix.
     * @param translations      a map of the translations for the methods.
     *
     * @return the class model used to create translation implementations of the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or {@link MessageLogger @MessageLogger}
     */
    public static ClassModel translation(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface, final String translationSuffix, final Map<MessageMethod, String> translations) throws IllegalArgumentException {
        final String generatedClassName = implementationClassName(messageInterface, translationSuffix);
        final String superClassName = getEnclosingTranslationClassName(generatedClassName);
        // The locale should be the same as the translationsSuffix minus the leading _
        final String locale = translationSuffix.substring(1);
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            return new MessageBundleTranslator(processingEnv, messageInterface, generatedClassName, superClassName, locale, translations);
        }
        if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            return new MessageLoggerTranslator(processingEnv, messageInterface, generatedClassName, superClassName, locale, translations);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }

    public static InterfaceModel translationI18n(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface, final Map<String,Map<MessageMethod, String>>  translations) throws IllegalArgumentException {
        final String generatedClassName = implementationClassName(messageInterface, "i18n");
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            return new XMessageBundleTranslator(processingEnv, messageInterface, generatedClassName, translations);
        }
        if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            return new XMessageLoggerTranslator(processingEnv, messageInterface, generatedClassName, translations);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }
}
