/*
 * TextComponentTranslator.java
 *
 * Mad Physicist JForm Project
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 by Joseph Fox-Rabinovitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.madphysicist.jform.translate;

import javax.swing.text.JTextComponent;

/**
 * Base class of simple translators for Swing text components. Subclasses of
 * this class operate on any {@code JTextComponent}.
 *
 * @param <T> The type of object read from {@code JTextComponents} translated by
 * extensions of this class.
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 26 May 2013: Joseph Fox-Rabinovitz: Created
 */
public abstract class TextComponentTranslator<T> implements Translator<T, JTextComponent>
{
    /**
     * Writes a {@code String} representation of the specified value into the
     * specified {@code JTextComponent}.
     *
     * @param value {@inheritDoc}. The string representation a value of any type
     * will be retrieved using the {@code toString()} method.
     * @param uiComponent {@inheritDoc}
     * @throws TranslatorException if the value is incompatible with the
     * specified component. This implementation does not throw any exceptions,
     * but allows subclasses to do so.
     * @since 1.0.0
     */
    @Override public void writeValue(Object value, JTextComponent uiComponent) throws TranslatorException
    {
        uiComponent.setText(value.toString());
    }

    /**
     * Returns a string representation of the text component if and only if it
     * could be read with {@code readValue()}. The raw value is converted into a
     * string with its {@code toString()} method.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException if the value can not be translated. This
     * implementation passes through any exceptions thrown by {@code
     * readValue()}.
     * @see #readValue(JTextComponent)
     * @since 1.0.0
     */
    @Override public java.lang.String readString(JTextComponent uiComponent) throws TranslatorException
    {
        return readValue(uiComponent).toString();
    }

    /**
     * {@inheritDoc} The value is parsed according to the requirements of the
     * subclass. If the returned object is not readily converted into a {@code
     * String} with its {@code toString()} method, the subclass should also
     * override the {@code readString()} method.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException {@inheritDoc}
     * @see #readString(JTextComponent)
     * @since 1.0.0
     */
    @Override public abstract T readValue(JTextComponent uiComponent) throws TranslatorException;

    /**
     * Reads strings from text components and vice versa. This class follows a
     * singleton pattern because it has no state.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 26 May 2013
     * @since 1.0.0
     */
    @SuppressWarnings("PublicInnerClass")
    public static class String extends TextComponentTranslator<java.lang.String>
    {
        /**
         * The version ID for serialization.
         *
         * @serial Increment the least significant three digits when
         * compatibility is not compromised by a structural change (e.g. adding
         * a new field with a sensible default value), and the upper digits when
         * the change makes serialized versions of of the class incompatible
         * with previous releases.
         * @since 1.0.0
         */
        private static final long serialVersionUID = 1000L;

        /**
         * The singleton instance of this class.
         *
         * @see #getInstance()
         * @since 1.0.0
         */
        private static final String instance = new String();

        /**
         * An empty constructor. This constructor is not private to allow
         * subclassing.
         *
         * @since 1.0.0
         */
        protected String() {}

        /**
         * Returns the raw contents of the text component. This method yields
         * identical results to {@code readString()}.
         *
         * @param uiComponent {@inheritDoc}
         * @return {@inheritDoc}
         * @throws TranslatorException {@inheritDoc}
         * @see #readString(JTextComponent)
         * @since 1.0.0
         */
        @Override public java.lang.String readValue(JTextComponent uiComponent) throws TranslatorException
        {
            return uiComponent.getText();
        }

        /**
         * Retrieves the singleton instance of this class.
         *
         * @return the singleton of this class.
         * @since 1.0.0
         */
        public static synchronized String getInstance()
        {
            return instance;
        }
    }

    /**
     * Reads integer values from text components and vice versa.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 26 May 2013
     * @since 1.0.0
     */
    @SuppressWarnings("PublicInnerClass")
    public static class Integer extends TextComponentTranslator<java.lang.Integer>
    {
        /**
         * The version ID for serialization.
         *
         * @serial Increment the least significant three digits when
         * compatibility is not compromised by a structural change (e.g. adding
         * a new field with a sensible default value), and the upper digits when
         * the change makes serialized versions of of the class incompatible
         * with previous releases.
         * @since 1.0.0
         */
        private static final long serialVersionUID = 1000L;

        /**
         * The singleton instance of this class.
         *
         * @see #getInstance()
         * @since 1.0.0
         */
        private static final Integer instance = new Integer();

        /**
         * An empty constructor. This constructor is not private to allow
         * subclassing.
         *
         * @since 1.0.0
         */
        protected Integer() {}

        /**
         * Retrieves an integer representation of the component's text. An
         * exception is thrown if the text can not be parsed as an integer. Note
         * that {@code writeValue()} still allows any string to be written to
         * the component.
         *
         * @param uiComponent {@inheritDoc}
         * @return the contents of the text field converted into a {@code
         * java.lang.Integer} object.
         * @throws TranslatorException if the contents of the text component
         * could not be parsed as an integer.
         * @see Integer#parseInt(String)
         * @since 1.0.0
         */
        @Override public java.lang.Integer readValue(JTextComponent uiComponent) throws TranslatorException
        {
            java.lang.String text = uiComponent.getText();
            try {
                return java.lang.Integer.parseInt(text);
            } catch(NumberFormatException nfe) {
                throw new TranslatorException("Cannot convert \"" + text + "\" to integer", nfe, this, uiComponent);
            }
        }

        /**
         * Retrieves the singleton instance of this class.
         *
         * @return the singleton of this class.
         * @since 1.0.0
         */
        public static synchronized Integer getInstance()
        {
            return instance;
        }
    }

    /**
     * Reads double precision floating point values from text components and
     * vice versa.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 26 May 2013
     * @since 1.0.0
     */
    @SuppressWarnings("PublicInnerClass")
    public static class Double extends TextComponentTranslator<java.lang.Double>
    {
        /**
         * The version ID for serialization.
         *
         * @serial Increment the least significant three digits when
         * compatibility is not compromised by a structural change (e.g. adding
         * a new field with a sensible default value), and the upper digits when
         * the change makes serialized versions of of the class incompatible
         * with previous releases.
         * @since 1.0.0
         */
        private static final long serialVersionUID = 1000L;

        /**
         * The singleton instance of this class.
         *
         * @see #getInstance()
         * @since 1.0.0
         */
        private static final Double instance = new Double();

        /**
         * An empty constructor. This constructor is not private to allow
         * subclassing.
         *
         * @since 1.0.0
         */
        protected Double() {}

        /**
         * Retrieves a floating point representation of the component's text. An
         * exception is thrown if the text can not be parsed as a double. Note
         * that {@code writeValue()} still allows any string to be written to
         * the component.
         *
         * @param uiComponent {@inheritDoc}
         * @return the contents of the text field converted into a {@code
         * java.lang.Double} object.
         * @throws TranslatorException if the contents of the text component
         * could not be parsed as a double.
         * @see Double#parseDouble(String)
         * @since 1.0.0
         */
        @Override public java.lang.Double readValue(JTextComponent uiComponent) throws TranslatorException
        {
            java.lang.String text = uiComponent.getText();
            try {
                return java.lang.Double.parseDouble(text);
            } catch(NumberFormatException nfe) {
                throw new TranslatorException("Cannot convert \"" + text + "\" to double", nfe, this, uiComponent);
            }
        }

        /**
         * Retrieves the singleton instance of this class.
         *
         * @return the singleton of this class.
         * @since 1.0.0
         */
        public static synchronized Double getInstance()
        {
            return instance;
        }
    }
}
