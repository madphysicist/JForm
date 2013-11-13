/*
 * FileFieldTranslator.java
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

import com.madphysicist.tools.swing.JFileField;

/**
 * Base class of all translators for file fields.
 *
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 28 May 2013
 * @since 1.0.0
 */
public class FileFieldTranslator implements Translator<java.io.File, JFileField>
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
     * @serial
     * @since 1.0.0.0
     */
    private boolean absolute;

    /**
     * @serial
     * @since 1.0.0.0
     */
    private boolean exists;

    /**
     * @serial
     * @since 1.0.0.0
     */
    private boolean parentExists;

    public FileFieldTranslator(boolean absolute, boolean exists, boolean parentExists)
    {
        this.absolute = absolute;
        this.exists = exists;
        this.parentExists = parentExists;
    }

    public boolean isExistanceRequired()
    {
        return exists;
    }

    public boolean isParentExistanceRequired()
    {
        return parentExists;
    }

    public boolean isAbsolute()
    {
        return absolute;
    }

    @Override public void writeValue(Object value, JFileField uiComponent) throws TranslatorException
    {
        if(value instanceof java.io.File)
            uiComponent.setText(((java.io.File)value).getPath());
        else if(value instanceof String)
            uiComponent.setText((String)value);
        else
            throw new TranslatorException("Incompatible value", this, uiComponent);
    }

    @Override public java.io.File readValue(JFileField uiComponent) throws TranslatorException
    {
        java.io.File file = (absolute) ? uiComponent.getAbsoluteFile() : uiComponent.getFile();
        if(parentExists) {
            java.io.File parent = file.getParentFile();
            if(!parent.isDirectory()) {
                throw new TranslatorException("Required parent directory " + parent.getPath()
                        + (parent.isFile() ? " exists as file" : " does not exist"),
                        this, uiComponent);
            }
        }
        if(exists && !file.exists()) {
            throw new TranslatorException("Required path " + file.getPath() + " does not exist", this, uiComponent);
        }
        return file;
    }

    @Override public String readString(JFileField uiComponent) throws TranslatorException
    {
        return readValue(uiComponent).getPath();
    }

    /**
     * Reads a File object representing strictly a file (not a directory) from a
     * file field and vice versa.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 27 May 2013
     * @since 1.0.0
     */
    @SuppressWarnings("PublicInnerClass")
    public static class File extends FileFieldTranslator
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

        public File(boolean absolute, boolean exists, boolean parentExists)
        {
            super(absolute, exists, parentExists);
        }

        @Override public java.io.File readValue(JFileField uiComponent) throws TranslatorException
        {
            java.io.File file = super.readValue(uiComponent);
            if(isExistanceRequired() && !file.isFile()) {
                throw new TranslatorException("File " + file.getPath() + " exists as directory", this, uiComponent);
            }
            return file;
        }
    }

    /**
     * Reads a File object representing strictly a directory (not a regular
     * file) from a file field and vice versa.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 28 May 2013
     * @since 1.0.0
     */
    @SuppressWarnings("PublicInnerClass")
    public static class Directory extends FileFieldTranslator
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

        public Directory(boolean absolute, boolean exists, boolean parentExists)
        {
            super(absolute, exists, parentExists);
        }

        @Override public java.io.File readValue(JFileField uiComponent) throws TranslatorException
        {
            java.io.File file = super.readValue(uiComponent);
            if(isExistanceRequired() && !file.isDirectory()) {
                throw new TranslatorException("Directory " + file.getPath() + " exists as file", this, uiComponent);
            }
            return file;
        }
    }
}
