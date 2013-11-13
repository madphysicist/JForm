/*
 * package-info.java
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

/**
 * A library for GUI-based configurations in Java. The "main" class of this
 * package is {@code JFormManager}. It allows the user to register {@code
 * Component}s that provide a user interface to the configuration properties.
 * <p>
 * {@code Converter} objects define a two way relationship between the UI
 * component and the underlying data that is extracted from it. It allows errors
 * to be marked on the components by changing the forground and/or background
 * colors of the component itself and surrounding components.
 * <p>
 * {@code Converter} uses an underlying {@code Translator} instance to convert
 * user-edited values into configuration properties and vice-versa. Translators
 * provide a way of writing {@code String}s and other objects out into a GUI
 * form and retrieving {@code String}s and other objects back out of the form.
 * <p>
 * A standard set of translators and markup instructions can be bound into a
 * {@code Converter} using a {@code ConverterFactory}. The default factory
 * contains a set of sensible values that are applicable to most situations.
 * Other factories can be created to extend the functionality of the default
 * one.
 *
 * @see com.madphysicist.jform.demo
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 */
package com.madphysicist.jform;
