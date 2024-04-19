/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.ibatis.BaseDataTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  void shouldGetUrlForResource() throws Exception {
    URL url = Resources.getResourceURL(JPETSTORE_PROPERTIES);
    assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetUrlAsProperties() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = Resources.getUrlAsProperties(url.toString());
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  void shouldGetResourceAsProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  void shouldGetUrlAsStream() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (InputStream in = Resources.getUrlAsStream(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetUrlAsReader() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (Reader in = Resources.getUrlAsReader(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsStream() throws Exception {
    try (InputStream in = Resources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsReader() throws Exception {
    try (Reader in = Resources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsFile() throws Exception {
    File file = Resources.getResourceAsFile(JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = Resources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetResourceAsPropertiesWithOutClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  void shouldGetResourceAsPropertiesWithClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  void shouldAllowDefaultClassLoaderToBeSet() {
    Resources.setDefaultClassLoader(this.getClass().getClassLoader());
    assertEquals(this.getClass().getClassLoader(), Resources.getDefaultClassLoader());
  }

  @Test
  void shouldAllowDefaultCharsetToBeSet() {
    Resources.setCharset(Charset.defaultCharset());
    assertEquals(Charset.defaultCharset(), Resources.getCharset());
  }

  @Test
  void shouldGetClassForName() throws Exception {
    Class<?> clazz = Resources.classForName(ResourcesTest.class.getName());
    assertNotNull(clazz);
  }

  @Test
  void shouldNotFindThisClass() {
    Assertions.assertThrows(ClassNotFoundException.class,
        () -> Resources.classForName("some.random.class.that.does.not.Exist"));
  }

  @Test
  void shouldGetReader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }

  @Test
  void shouldGetReaderWithClassLoader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }
  
  // 以下用于理解 URL，ClassLoader，等概念：JVM的类加载机制，结合着理解学习
  @Test
  void testPath() throws IOException {
    // 加 / 的意义是？ now, some class loaders want this leading "/", so we'll add it and try again if we didn't find the resource
    // 处理的是类加载器的原因（部分类加载器需要 前缀，并非是全路径
    try(InputStream in = Resources.getResourceAsStream(PATH_TEST)){
      assertNotNull(in);
    }
  }
  
  @Test
  void testDirectClLoad() throws IOException {
    /**
     * ClassLoader 的 getResourceAsStream 方法加载资源时，路径通常是相对于类路径的根目录，
     * 而不是相对于某个特定类的位置。这意味着无法直接使用相对于某个类的位置的路径来加载资源。
     * 如果你想相对于某个类的位置加载资源，可以使用 Class 类的 getResourceAsStream 方法，该方法加载的资源路径是相对于类所在的包的位置
     */
//    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    InputStream resourceAsStream = contextClassLoader.getResourceAsStream(PATH_TEST);
//    assertNotNull(resourceAsStream);
    try(InputStream in = ResourcesTest.class.getResourceAsStream(PATH_TEST)){
      assertNotNull(in);
    }
  }

}
