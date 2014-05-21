/*
 * SpinnerTranslator.java
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

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * Base class of simple translators for {@code JSpinner}s. Subclasses of this
 * class operate on {@code JSpinner} components with specific types of models.
 * All of the nested implementations provided require a {@code
 * SpinnerNumberModel}.
 *
 * @see SpinnerNumberModel
 * @param <T> The type of object read from {@code JSpinner}s translated by
 * extensions of this class.
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 20 June 2013: Joseph Fox-Rabinovitz: Created
 */
public abstract class SpinnerTranslator<T> implements Translator<T, JSpinner>
{
    /**
     * The version ID for serialization.
     *
     * @serial Increment the least significant three digits when compatibility
     * is not compromised by a structural change (e.g. adding a new field with
     * a sensible default value), and the upper digits when the change makes
     * serialized versions of of the class incompatible with previous releases.
     * @since 1.0.0
     */
	private static final long serialVersionUID = 1000L;

	/**
     * Returns a string representation of the value in the spinner. The value is
     * converted directly using its {@code toString()} method. Subclasses should
     * override this method if a less generic implementation is required.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException if {@code readValue()} throws an exception.
     * @since 1.0.0
     */
    @Override public String readString(JSpinner uiComponent) throws TranslatorException
    {
        return readValue(uiComponent).toString();
    }

    /**
     * Writes the specified value to the spinner. This method checks that the
     * type of the value is compatible with the spinner's model. Extending
     * classes should call this method within implementations of {@code
     * writeValue()} to convert the {@code IllegalArgumentException} thrown by
     * {@code SpinnerModel}s into a {@code TranslatorComponentException}.
     *
     * @param value the value to set. Must be compatible with the spinner's
     * model. This value may be a parsed version of the value passed in to
     * {@code writeValue()}.
     * @param uiComponent the spinner to set the value in.
     * @throws TranslatorComponentException if the value's type is incompatible
     * with the spinner's model.
     * @see SpinnerModel#setValue(Object)
     * @since 1.0.0
     */
    protected void writeCheckedValue(Object value, JSpinner uiComponent) throws TranslatorComponentException
    {
        try {
            uiComponent.setValue(value);
        } catch(IllegalArgumentException iae) {
            throw new TranslatorComponentException("Incompatible model", iae, this, uiComponent);
        }
    }

    /**
     * Reads a {@code Double} object from the spinner. This class can work with
     * all spinners having a {@code SpinnerNumberModel}. Other models may cause
     * a {@code TranslatorComponentException} if they do not contain exclusively
     * {@code Number}s. This class follows a singleton pattern because it has no
     * state.
     *
     * @see SpinnerNumberModel
     * @see TranslatorComponentException
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 20 June 2013: Joseph Fox-Rabinovitz: Created
     * @since 1.0.0
     */
    public static class Double extends SpinnerTranslator<java.lang.Double>
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
        private static Double instance = new Double();

        /**
         * An empty constructor. This constructor is not private to allow
         * subclassing.
         *
         * @since 1.0.0
         */
        protected Double() {}

        /**
         * Writes a value to the spinner. The value is required to be a {@code
         * Number} or a {@code String} which can be parsed into a {@code
         * double}. This method only allows values that can be read by either
         * {@code readValue()} or {@code readString()} to be written.
         *
         * @param value {@inheritDoc}. Must be a {@code Number} or a parseable
         * {@code String}.
         * @param uiComponent {@inheritDoc}
         * @throws TranslatorException if the value is not of an allowed type or
         * does not parse into a number.
         * @see Double#parseDouble(String)
         * @since 1.0.0
         */
        @Override public void writeValue(Object value, JSpinner uiComponent) throws TranslatorException
        {
            if(value instanceof Number) {
                super.writeCheckedValue(value, uiComponent);
            } else if(value instanceof String) {
                try {
                    super.writeCheckedValue(java.lang.Double.valueOf((String)value), uiComponent);
                } catch(NumberFormatException nfe) {
                    throw new TranslatorException("Not a double: " + value, nfe, this, uiComponent);
                }
            } else {
                throw new TranslatorException("Incompatible value", this, uiComponent);
            }
        }

        /**
         * Reads a floating point value from the spinner. The spinner model must
         * contain {@code Number}s.
         *
         * @param uiComponent {@inheritDoc}
         * @return {@inheritDoc}
         * @throws TranslatorException this method does not throw this
         * particular exception. The {@code throws} clause is provided so that
         * subclasses can throw it.
         * @throws TranslatorComponentException if the model contains a
         * non-{@code Number} value in its current state.
         * @since 1.0.0
         */
        @Override public java.lang.Double readValue(JSpinner uiComponent) throws TranslatorException, TranslatorComponentException
        {
            Object value = uiComponent.getValue();

            if(value instanceof java.lang.Double) {
                return (java.lang.Double)value;
            } else if(value instanceof Number) {
                return java.lang.Double.valueOf(((Number)value).doubleValue());
            } else {
                throw new TranslatorComponentException("Incompatible model", this, uiComponent);
            }
        }

        /**
         * Retrieves the singleton instance of this class.
         *
         * @return the singleton of this class.
         * @since 1.0.0
         */
        public static Double getInstance()
        {
            return instance;
        }
    }

    /**
     * Reads an {@code Integer} object from the spinner. This class can work
     * with all spinners having a {@code SpinnerNumberModel}. Other models may
     * cause a {@code TranslatorComponentException} if they do not contain
     * exclusively {@code Number}s. This class follows a singleton pattern
     * because it has no state.
     *
     * @see SpinnerNumberModel
     * @see TranslatorComponentException
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 20 June 2013: Joseph Fox-Rabinovitz: Created
     * @since 1.0.0
     */
    public static class Integer extends SpinnerTranslator<java.lang.Integer>
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
        private static Integer instance = new Integer();

        /**
         * An empty constructor. This constructor is not private to allow
         * subclassing.
         *
         * @since 1.0.0
         */
        protected Integer() {}

        /**
         * Writes a value to the spinner. The value is required to be a {@code
         * Number} or a {@code String} which can be parsed into an {@code int}.
         * This method only allows values that can be read by either {@code
         * readValue()} or {@code readString()} to be written.
         *
         * @param value {@inheritDoc}. Must be a {@code Number} or a parseable
         * {@code String}.
         * @param uiComponent {@inheritDoc}
         * @throws TranslatorException if the value is not of an allowed type or
         * does not parse into a number.
         * @see Integer#parseInt(String)
         * @since 1.0.0
         */
        @Override public void writeValue(Object value, JSpinner uiComponent) throws TranslatorException
        {
            if(value instanceof Number) {
                super.writeCheckedValue(value, uiComponent);
            } else if(value instanceof String) {
                try {
                    super.writeCheckedValue(java.lang.Integer.valueOf((String)value), uiComponent);
                } catch(NumberFormatException nfe) {
                    throw new TranslatorException("Not an integer: " + value, nfe, this, uiComponent);
                }
            }
            throw new TranslatorException("Incompatible value", this, uiComponent);
        }

        /**
         * Reads an integer value from the spinner. The spinner model must
         * contain {@code Number}s.
         *
         * @param uiComponent {@inheritDoc}
         * @return {@inheritDoc}
         * @throws TranslatorException this method does not throw this
         * particular exception. The {@code throws} clause is provided so that
         * subclasses can throw it.
         * @throws TranslatorComponentException if the model contains a
         * non-{@code Number} value in its current state.
         * @since 1.0.0
         */
        @Override public java.lang.Integer readValue(JSpinner uiComponent) throws TranslatorException, TranslatorComponentException
        {
            Object value = uiComponent.getValue();
            if(value instanceof java.lang.Integer)
                return (java.lang.Integer)value;
            else if(value instanceof Number)
                return java.lang.Integer.valueOf(((Number)value).intValue());
            else
                throw new TranslatorComponentException("Incompatible model", this, uiComponent);
        }

        /**
         * Retrieves the singleton instance of this class.
         *
         * @return the singleton of this class.
         * @since 1.0.0
         */
        public static Integer getInstance()
        {
            return instance;
        }
    }
}
