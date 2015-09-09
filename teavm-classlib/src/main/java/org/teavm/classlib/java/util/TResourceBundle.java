/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.teavm.classlib.java.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.harmony.luni.internal.nls.Messages;

/**
 * {@code ResourceBundle} is an abstract class which is the superclass of classes which
 * provide {@code Locale}-specific resources. A bundle contains a number of named
 * resources, where the names are {@code Strings}. A bundle may have a parent bundle,
 * and when a resource is not found in a bundle, the parent bundle is searched for
 * the resource. If the fallback mechanism reaches the base bundle and still
 * can't find the resource it throws a {@code MissingResourceException}.
 *
 * <ul>
 * <li>All bundles for the same group of resources share a common base bundle.
 * This base bundle acts as the root and is the last fallback in case none of
 * its children was able to respond to a request.</li>
 * <li>The first level contains changes between different languages. Only the
 * differences between a language and the language of the base bundle need to be
 * handled by a language-specific {@code ResourceBundle}.</li>
 * <li>The second level contains changes between different countries that use
 * the same language. Only the differences between a country and the country of
 * the language bundle need to be handled by a country-specific {@code ResourceBundle}.
 * </li>
 * <li>The third level contains changes that don't have a geographic reason
 * (e.g. changes that where made at some point in time like {@code PREEURO} where the
 * currency of come countries changed. The country bundle would return the
 * current currency (Euro) and the {@code PREEURO} variant bundle would return the old
 * currency (e.g. DM for Germany).</li>
 * </ul>
 *
 * <strong>Examples</strong>
 * <ul>
 * <li>BaseName (base bundle)
 * <li>BaseName_de (german language bundle)
 * <li>BaseName_fr (french language bundle)
 * <li>BaseName_de_DE (bundle with Germany specific resources in german)
 * <li>BaseName_de_CH (bundle with Switzerland specific resources in german)
 * <li>BaseName_fr_CH (bundle with Switzerland specific resources in french)
 * <li>BaseName_de_DE_PREEURO (bundle with Germany specific resources in german of
 * the time before the Euro)
 * <li>BaseName_fr_FR_PREEURO (bundle with France specific resources in french of
 * the time before the Euro)
 * </ul>
 *
 * It's also possible to create variants for languages or countries. This can be
 * done by just skipping the country or language abbreviation:
 * BaseName_us__POSIX or BaseName__DE_PREEURO. But it's not allowed to
 * circumvent both language and country: BaseName___VARIANT is illegal.
 *
 * @see Properties
 * @see TPropertyResourceBundle
 * @see ListResourceBundle
 * @since 1.1
 */
public abstract class TResourceBundle {

    /**
     * The parent of this {@code ResourceBundle} that is used if this bundle doesn't
     * include the requested resource.
     */
    protected TResourceBundle parent;

    private Locale locale;

    static class MissingBundle extends TResourceBundle {
        @Override
        public Enumeration<String> getKeys() {
            return null;
        }

        @Override
        public Object handleGetObject(String name) {
            return null;
        }
    }

    private static final TResourceBundle MISSING = new MissingBundle();

    private static final TResourceBundle MISSINGBASE = new MissingBundle();

    private static final TWeakHashMap<Object, THashtable<String, TResourceBundle>> cache = new TWeakHashMap<Object, THashtable<String, TResourceBundle>>();

    /**
     * Constructs a new instance of this class.
     */
    public TResourceBundle() {
        /* empty */
    }

    /**
     * Finds the named resource bundle for the default {@code Locale} and the caller's
     * {@code ClassLoader}.
     * 
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @return the requested {@code ResourceBundle}.
     * @throws MissingResourceException
     *                if the {@code ResourceBundle} cannot be found.
     */
    public static final TResourceBundle getBundle(String bundleName)
            throws MissingResourceException {
        return getBundleImpl(bundleName, Locale.getDefault(), null);
    }

    /**
     * Finds the named {@code ResourceBundle} for the specified {@code Locale} and the caller
     * {@code ClassLoader}.
     * 
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @param locale
     *            the {@code Locale}.
     * @return the requested resource bundle.
     * @throws MissingResourceException
     *                if the resource bundle cannot be found.
     */
    public static final TResourceBundle getBundle(String bundleName,
            Locale locale) {
        return getBundleImpl(bundleName, locale, null);
    }

    /**
     * Finds the named resource bundle for the specified {@code Locale} and {@code ClassLoader}.
     * 
     * The passed base name and {@code Locale} are used to create resource bundle names.
     * The first name is created by concatenating the base name with the result
     * of {@link Locale#toString()}. From this name all parent bundle names are
     * derived. Then the same thing is done for the default {@code Locale}. This results
     * in a list of possible bundle names.
     *
     * <strong>Example</strong> For the basename "BaseName", the {@code Locale} of the
     * German part of Switzerland (de_CH) and the default {@code Locale} en_US the list
     * would look something like this:
     *
     * <ol>
     * <li>BaseName_de_CH</li>
     * <li>BaseName_de</li>
     * <li>Basename_en_US</li>
     * <li>Basename_en</li>
     * <li>BaseName</li>
     * </ol>
     *
     * This list also shows the order in which the bundles will be searched for a requested
     * resource in the German part of Switzerland (de_CH).
     *
     * As a first step, this method tries to instantiate
     * a {@code ResourceBundle} with the names provided.
     * If such a class can be instantiated and initialized, it is returned and
     * all the parent bundles are instantiated too. If no such class can be
     * found this method tries to load a {@code .properties} file with the names by
     * replacing dots in the base name with a slash and by appending
     * "{@code .properties}" at the end of the string. If such a resource can be found
     * by calling {@link ClassLoader#getResource(String)} it is used to
     * initialize a {@link TPropertyResourceBundle}. If this succeeds, it will
     * also load the parents of this {@code ResourceBundle}.
     *
     * For compatibility with older code, the bundle name isn't required to be
     * a fully qualified class name. It's also possible to directly pass
     * the path to a properties file (without a file extension).
     *
     * @param bundleName
     *            the name of the {@code ResourceBundle}.
     * @param locale
     *            the {@code Locale}.
     * @param loader
     *            the {@code ClassLoader} to use.
     * @return the requested {@code ResourceBundle}.
     * @throws MissingResourceException
     *                if the {@code ResourceBundle} cannot be found.
     */
    public static TResourceBundle getBundle(String bundleName, Locale locale,
            ClassLoader loader) throws MissingResourceException {
        if (loader == null) {
            throw new NullPointerException();
        }
        if (bundleName != null) {
            TResourceBundle bundle;
            if (!locale.equals(Locale.getDefault())) {
                if ((bundle = handleGetBundle(bundleName, "_" + locale, false, //$NON-NLS-1$
                        loader)) != null) {
                    return bundle;
                }
            }
            if ((bundle = handleGetBundle(bundleName,
                    "_" + Locale.getDefault(), true, loader)) != null) { //$NON-NLS-1$
                return bundle;
            }
            throw new MissingResourceException(Messages.getString("luni.3A", bundleName, locale), bundleName + '_' + locale, //$NON-NLS-1$
                    ""); //$NON-NLS-1$
        }
        throw new NullPointerException();
    }

    private static TResourceBundle getBundleImpl(String bundleName,
            Locale locale, ClassLoader loader) throws MissingResourceException {
        if (bundleName != null) {
            TResourceBundle bundle;
            if (!locale.equals(Locale.getDefault())) {
                String localeName = locale.toString();
                if (localeName.length() > 0) {
                    localeName = "_" + localeName; //$NON-NLS-1$
                }
                if ((bundle = handleGetBundle(bundleName, localeName, false,
                        loader)) != null) {
                    return bundle;
                }
            }
            String localeName = Locale.getDefault().toString();
            if (localeName.length() > 0) {
                localeName = "_" + localeName; //$NON-NLS-1$
            }
            if ((bundle = handleGetBundle(bundleName, localeName, true, loader)) != null) {
                return bundle;
            }
            throw new MissingResourceException(Messages.getString("luni.3A", bundleName, locale), bundleName + '_' + locale, //$NON-NLS-1$
                    ""); //$NON-NLS-1$
        }
        throw new NullPointerException();
    }

    /**
     * Returns the names of the resources contained in this {@code ResourceBundle}.
     * 
     * @return an {@code Enumeration} of the resource names.
     */
    public abstract Enumeration<String> getKeys();

    /**
     * Gets the {@code Locale} of this {@code ResourceBundle}. In case a bundle was not
     * found for the requested {@code Locale}, this will return the actual {@code Locale} of
     * this resource bundle that was found after doing a fallback.
     * 
     * @return the {@code Locale} of this {@code ResourceBundle}.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}. If the resource
     * cannot be found in this bundle, it falls back to the parent bundle (if
     * it's not null) by calling the {@link #handleGetObject} method. If the resource still
     * can't be found it throws a {@code MissingResourceException}.
     * 
     * @param key
     *            the name of the resource.
     * @return the resource object.
     * @throws MissingResourceException
     *                if the resource is not found.
     */
    public final Object getObject(String key) {
        TResourceBundle last, theParent = this;
        do {
            Object result = theParent.handleGetObject(key);
            if (result != null) {
                return result;
            }
            last = theParent;
            theParent = theParent.parent;
        } while (theParent != null);
        throw new MissingResourceException(Messages.getString("luni.3A", last.getClass().getName(), key), last.getClass().getName(), key); //$NON-NLS-1$
    }

    /**
     * Returns the named string resource from this {@code ResourceBundle}.
     * 
     * @param key
     *            the name of the resource.
     * @return the resource string.
     * @throws MissingResourceException
     *                if the resource is not found.
     * @throws ClassCastException
     *                if the resource found is not a string.
     * @see #getObject(String)
     */
    public final String getString(String key) {
        return (String) getObject(key);
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}.
     * 
     * @param key
     *            the name of the resource.
     * @return the resource string array.
     * @throws MissingResourceException
     *                if the resource is not found.
     * @throws ClassCastException
     *                if the resource found is not an array of strings.
     * @see #getObject(String)
     */
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    private static TResourceBundle handleGetBundle(String base, String locale, boolean loadBase, final ClassLoader loader) {
        TResourceBundle bundle = null;
        String bundleName = base + locale;
        Object cacheKey = loader != null ? (Object) loader : (Object) "null"; //$NON-NLS-1$
        THashtable<String, TResourceBundle> loaderCache;
        synchronized (cache) {
            loaderCache = cache.get(cacheKey);
            if (loaderCache == null) {
                loaderCache = new THashtable<String, TResourceBundle>();
                cache.put(cacheKey, loaderCache);
            }
        }
        TResourceBundle result = loaderCache.get(bundleName);
        if (result != null) {
            if (result == MISSINGBASE) {
                return null;
            }
            if (result == MISSING) {
                if (!loadBase) {
                    return null;
                }
                String extension = strip(locale);
                if (extension == null) {
                    return null;
                }
                return handleGetBundle(base, extension, loadBase, loader);
            }
            return result;
        }

        try {
            Class<?> bundleClass = Class.forName(bundleName, true, loader);

            if (TResourceBundle.class.isAssignableFrom(bundleClass)) {
                bundle = (TResourceBundle) bundleClass.newInstance();
            }
        } catch (LinkageError e) {
        } catch (Exception e) {
        }

        if (bundle != null) {
            bundle.setLocale(locale);
        } else {
            final String fileName = bundleName.replace('.', '/');
            InputStream stream = AccessController
                    .doPrivileged(new PrivilegedAction<InputStream>() {
                        public InputStream run() {
                            return loader == null ? ClassLoader
                                    .getSystemResourceAsStream(fileName
                                            + ".properties") : loader //$NON-NLS-1$
                                    .getResourceAsStream(fileName
                                            + ".properties"); //$NON-NLS-1$
                        }
                    });
            if (stream != null) {
                try {
                    try {
                        bundle = new TPropertyResourceBundle(stream);
                    } finally {
                        stream.close();
                    }
                    bundle.setLocale(locale);
                } catch (IOException e) {
                }
            }
        }

        String extension = strip(locale);
        if (bundle != null) {
            if (extension != null) {
                TResourceBundle parent = handleGetBundle(base, extension, true,
                        loader);
                if (parent != null) {
                    bundle.setParent(parent);
                }
            }
            loaderCache.put(bundleName, bundle);
            return bundle;
        }

        if (extension != null && (loadBase || extension.length() > 0)) {
            bundle = handleGetBundle(base, extension, loadBase, loader);
            if (bundle != null) {
                loaderCache.put(bundleName, bundle);
                return bundle;
            }
        }
        loaderCache.put(bundleName, loadBase ? MISSINGBASE : MISSING);
        return null;
    }

    /**
     * Returns the named resource from this {@code ResourceBundle}, or null if the
     * resource is not found.
     * 
     * @param key
     *            the name of the resource.
     * @return the resource object.
     */
    protected abstract Object handleGetObject(String key);

    /**
     * Sets the parent resource bundle of this {@code ResourceBundle}. The parent is
     * searched for resources which are not found in this {@code ResourceBundle}.
     * 
     * @param bundle
     *            the parent {@code ResourceBundle}.
     */
    protected void setParent(TResourceBundle bundle) {
        parent = bundle;
    }

    private static String strip(String name) {
        int index = name.lastIndexOf('_');
        if (index != -1) {
            return name.substring(0, index);
        }
        return null;
    }

    private void setLocale(String name) {
        String language = "", country = "", variant = ""; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        if (name.length() > 1) {
            int nextIndex = name.indexOf('_', 1);
            if (nextIndex == -1) {
                nextIndex = name.length();
            }
            language = name.substring(1, nextIndex);
            if (nextIndex + 1 < name.length()) {
                int index = nextIndex;
                nextIndex = name.indexOf('_', nextIndex + 1);
                if (nextIndex == -1) {
                    nextIndex = name.length();
                }
                country = name.substring(index + 1, nextIndex);
                if (nextIndex + 1 < name.length()) {
                    variant = name.substring(nextIndex + 1, name.length());
                }
            }
        }
        locale = new Locale(language, country, variant);
    }
}
