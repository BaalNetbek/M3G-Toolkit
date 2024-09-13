package com.wizzer.m3g.midp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
   public static Object callMethod(Object object, String methodName, Object[] args) {
      return callMethod(object, methodName, (Class[])null, args);
   }

   public static Object callMethod(Object object, String methodName, Class[] methodArgTypes, Object[] args) {
      Object result = null;
      Method method = findMethod(object, methodName, methodArgTypes);
      if (method != null) {
         boolean access = method.isAccessible();
         method.setAccessible(true);

         try {
            result = method.invoke(object, args);
         } catch (Exception var11) {
            var11.printStackTrace();
         } finally {
            method.setAccessible(access);
         }
      }

      return result;
   }

   public static Method findMethod(Object object, String methodName, Class[] methodArgTypes) {
      Class c = object.getClass();
      Class objClass = null;

      try {
         objClass = Class.forName("java.lang.Object");
      } catch (ClassNotFoundException var6) {
         return null;
      }

      while(c != objClass) {
         try {
            Method method = c.getDeclaredMethod(methodName, methodArgTypes);
            return method;
         } catch (NoSuchMethodException var7) {
            c = c.getSuperclass();
         }
      }

      return null;
   }

   public static Object getField(Object obj, String name) {
      Object result = null;
      Field f = findField(obj, name);
      if (f != null) {
         boolean access = f.isAccessible();
         f.setAccessible(true);

         try {
            result = f.get(obj);
         } catch (Exception var9) {
            var9.printStackTrace();
         } finally {
            f.setAccessible(access);
         }
      }

      return result;
   }

   public static Field findField(Object obj, String field) {
      Class c = obj.getClass();
      Class objClass = null;

      try {
         objClass = Class.forName("java.lang.Object");
      } catch (ClassNotFoundException var5) {
         return null;
      }

      while(c != objClass) {
         try {
            Field f = c.getDeclaredField(field);
            return f;
         } catch (NoSuchFieldException var6) {
            c = c.getSuperclass();
         }
      }

      return null;
   }
}
