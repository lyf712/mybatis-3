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
package org.apache.ibatis.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.logging.LogException;
import org.apache.ibatis.parsing.ParsingException;
import org.apache.ibatis.plugin.PluginException;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.session.SqlSessionException;
import org.apache.ibatis.transaction.TransactionException;
import org.apache.ibatis.type.TypeException;
import org.junit.jupiter.api.Test;

class GeneralExceptionsTest {

  private static final String EXPECTED_MESSAGE = "Test Message";
  private static final Exception EXPECTED_CAUSE = new Exception("Nested Exception");

  @Test
  void should() {
    // 关注一下：ErrorContext类，toString 有点Builder模式的设计感觉，在 + 拼接字符串，自己调用toString函数
    // 其采用ThreadLocal的设计，可以学习思考
    RuntimeException thrown = ExceptionFactory.wrapException(EXPECTED_MESSAGE, EXPECTED_CAUSE);
    assertTrue(thrown instanceof PersistenceException, "Exception should be wrapped in RuntimeSqlException.");
    testThrowException(thrown);
  }

  @Test
  void shouldInstantiateAndThrowAllCustomExceptions() throws Exception {
    // 采用反射，来创建调用构造函数，避免反复创建new -- 冗余
    //
    Class<?>[] exceptionTypes = { BindingException.class, CacheException.class, DataSourceException.class,
        ExecutorException.class, LogException.class, ParsingException.class, BuilderException.class,
        PluginException.class, ReflectionException.class, PersistenceException.class, SqlSessionException.class,
        TransactionException.class, TypeException.class, ScriptingException.class };
    for (Class<?> exceptionType : exceptionTypes) {
      testExceptionConstructors(exceptionType);
    }

  }

  private void testExceptionConstructors(Class<?> exceptionType)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    Exception e = (Exception) exceptionType.getDeclaredConstructor().newInstance();
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class).newInstance(EXPECTED_MESSAGE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(String.class, Throwable.class).newInstance(EXPECTED_MESSAGE,
        EXPECTED_CAUSE);
    testThrowException(e);
    e = (Exception) exceptionType.getConstructor(Throwable.class).newInstance(EXPECTED_CAUSE);
    testThrowException(e);
  }

  private void testThrowException(Exception thrown) {
    try {
      throw thrown;
    } catch (Exception caught) {
      System.out.println(thrown.getMessage());
      assertEquals(thrown.getMessage(), caught.getMessage());
      assertEquals(thrown.getCause(), caught.getCause());
    }
  }

  // 以下为个人的测试理解
  
  @Test
  public void test(){
    try {
      exception3();
    } catch (Exception e) {
      Exception e1 = e;
      // cause的stackTrace(StackTraceElemenet 追踪到 创建该异常的方法处
      // 对于new RuntimeException("Test Msg2",e); 在exception2（）内进行throw new xxx 因此追踪到该处
      // 对于它的cause则是 ，PersistenceE,则追踪到Factory
      // return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
      // 对于SQLXE,则是追踪到exception1()
      //throw new RuntimeException(e);
      System.out.println(e.getMessage());
    }
  }
  
  private void exception3() throws Exception{
    exception2();
  }
  
  private void exception2() throws Exception{
    try {
      exception1();
    }catch (Exception e){
      throw new RuntimeException("Test Msg2",e);
    }
   
  }
  
  private void exception1() throws PersistenceException{
    throw ExceptionFactory.wrapException("Test Msg1",new SQLException("SQL DOT ERROR"));
  }
}
