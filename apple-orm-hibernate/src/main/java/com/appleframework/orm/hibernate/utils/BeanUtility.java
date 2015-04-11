package com.appleframework.orm.hibernate.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;


public class BeanUtility {
	
	protected static Logger logger = Logger.getLogger(BeanUtility.class);

	/**
	 * 获得同时有get和set的field和value。
	 * 
	 * @param bean
	 * @return
	 */
	public static Map<String, Object> describe(Object bean) {
		Map<String, Object> des = new HashMap<String, Object>();
		PropertyDescriptor desor[] = PropertyUtils.getPropertyDescriptors(bean);
		String name = null;
		for (int i = 0; i < desor.length; i++) {
			if (desor[i].getReadMethod() != null) {
				name = desor[i].getName();
				try {
					des.put(name, PropertyUtils.getProperty(bean, name));
				} catch (Exception e) {
					throw new RuntimeException("属性不存在：" + name);
				}
			}
		}
		return des;
	}
	
	/**
	 * 获得同时有get和set的field和value。
	 * 
	 * @param bean
	 * @return
	 */
	public static Map<String, Object> describeForHQL(Object bean) {
		Map<String, Object> des = new HashMap<String, Object>();
		PropertyDescriptor desor[] = PropertyUtils.getPropertyDescriptors(bean);
		String name = null;
		for (int i = 0; i < desor.length; i++) {
			if (desor[i].getReadMethod() != null) {
				name = desor[i].getName();
				try {
					Object obj = PropertyUtils.getProperty(bean, name);
					if ((ObjectUtility.isNotEmpty(obj)) && (obj instanceof java.lang.String)){
						des.put(name, "%" + obj.toString().toLowerCase() + "%");
					}
					else{
						des.put(name, obj);
					}
				} catch (Exception e) {
					throw new RuntimeException("属性不存在：" + name);
				}
			}
		}
		return des;
	}

	public static void setSimpleProperty(Object bean, String name, Object value) {
		try {
			PropertyUtils.setSimpleProperty(bean, name, value);
		} catch (Exception e) {
			throw new RuntimeException("属性不存在：" + name);
		}
	}

	public static Object setSimpleProperty(Object bean, String name) {
		try {
			return PropertyUtils.getSimpleProperty(bean, name);
		} catch (Exception e) {
			throw new RuntimeException("属性不存在：" + name);
		}
	}

	/**
	 * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
	 */
	public static Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException {
		Field field = getDeclaredField(object, fieldName);
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			logger.error("不可能抛出的异常{}", e);
		}
		return result;
	}

	/**
	 * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
	 */
	public static void setFieldValue(Object object, String fieldName, Object value) throws NoSuchFieldException {
		Field field = getDeclaredField(object, fieldName);
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			logger.error("不可能抛出的异常:{}", e);
		}
	}

	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 */
	public static Field getDeclaredField(Object object, String fieldName) throws NoSuchFieldException {
		Assert.notNull(object);
		return getDeclaredField(object.getClass(), fieldName);
	}

	/**
	 * 循环向上转型,获取类的DeclaredField.
	 */
	public static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		Assert.notNull(clazz);
		Assert.hasText(fieldName);
		for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// Field不在当前类定义,继续向上转型
				logger.error(e.getMessage());
			}
		}
		throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + fieldName);
	}
	
	/**
	 *@since 2010-1-18 
	 *@author xusm
	 *@param beanClass
	 *@return
	 * 获得javabean中 所有属性的名字。
	 */
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static String[] getKeyArray(Object beanClass) {
        if(beanClass instanceof Map) {
        	Map mapTemp = (Map)beanClass;
        	String[] keyArray = new String[mapTemp.size()];
        	Set<?> set = mapTemp.entrySet();
        	Iterator<?> it = set.iterator();
        	int i =0;
        	while(it.hasNext()) {
        		Entry entry = (Entry)it.next();
        		keyArray[i] = entry.getKey().toString();
        		i++;
        	}
        	return keyArray;
        }
		// Look up the property descriptors for this bean class
		PropertyDescriptor regulars[] = PropertyUtils.getPropertyDescriptors(beanClass);
		if (regulars == null) {
			regulars = new PropertyDescriptor[0];
		}
		HashMap mappeds = PropertyUtils.getMappedPropertyDescriptors(beanClass);
		if (mappeds == null) {
			mappeds = new HashMap();
		}

		// Construct corresponding DynaProperty information
		String[] keyArray = new String[regulars.length + mappeds.size()];
		for (int i = 0; i < regulars.length; i++) {
			keyArray[i] = regulars[i].getName();
		}
		int j = regulars.length;
		Iterator names = mappeds.keySet().iterator();
		while (names.hasNext()) {
			String name = (String) names.next();
			PropertyDescriptor descriptor = (PropertyDescriptor) mappeds.get(name);
			keyArray[j] = descriptor.getName();
			j++;
		}
		return keyArray;
	}
	
	
	
	
	
	

	/**
	 * 
	 * 2010-1-18 xusm
	 * 
	 * @param bean
	 * @return 把一个普通的javabean 转化成map对象，属性名作为map的key，属性值作为map的value。
	 */
	public static Map<String, Object> convertToMap(Object bean) {
		String[] keyArray = BeanUtility.getKeyArray(bean);
		Map<String, Object> map = new HashMap<String, Object>();
		for (int j = 0; j < keyArray.length; j++) {
			if (!keyArray[j].equals("class")) {
				try {
					map.put(keyArray[j], PropertyUtils.getProperty(bean, keyArray[j]));
				} catch (IllegalAccessException e) {
					logger.error(e.getMessage());
				} catch (InvocationTargetException e) {
					logger.error(e.getMessage());
				} catch (NoSuchMethodException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return map;
	}

	/**
	 * 把map 对象转换为一个普通的javabean对象
	 * @param beanClass
	 * @param map
	 * @return
	 */
	public static Object convertToBean(Class<?> beanClass, Map<String, Object> map) {
		Object bean = null;
		try {
			bean = beanClass.newInstance();
			if (bean != null) {
				for (String key : map.keySet()) {
					String merthodName = "set"
							+ key.substring(0, 1).toUpperCase()
							+ key.substring(1);
					Method destMethod = null;
					Object srcValue = map.get(key);
					try {
						if (srcValue != null) {
							destMethod = beanClass.getMethod(merthodName, srcValue.getClass());
						}
					} catch (Exception ex) {
						continue;
					}
					if (destMethod == null) {
						continue;
					}
					try {
						destMethod.invoke(bean, srcValue);
					} catch (Exception ex) {
						continue;
					}
				}
			}
		} catch (InstantiationException e1) {
			logger.error(e1.getMessage());
		} catch (IllegalAccessException e2) {
			logger.error(e2.getMessage());
		}
		return bean;
	}

	/**
	 * 对象属性拷贝 不拷贝非null的属性
	 * 
	 * @param dest
	 *            目标对象
	 * @param src
	 *            源对象
	 */
	public static void copyProperties(Object dest, Object src) {
		PropertyUtilsBean util = new PropertyUtilsBean();
		PropertyDescriptor[] srcProps = util.getPropertyDescriptors(src.getClass());
		for (int i = 0; i < srcProps.length; i++) {
			Method srcMethod = srcProps[i].getReadMethod();
			Object srcValue = null;
			try {
				srcValue = srcMethod.invoke(src);
			} catch (Exception ex) {
				continue;
			}
			if (srcValue == null) {
				continue;
			}
			Class<?> type = srcProps[i].getPropertyType();
			String merthodName = "set"
					+ srcProps[i].getName().substring(0, 1).toUpperCase()
					+ srcProps[i].getName().substring(1);
			Method destMethod = null;
			try {
				destMethod = dest.getClass().getMethod(merthodName, type);
			} catch (Exception ex) {
				continue;
			}
			if (destMethod == null) {
				continue;
			}
			try {
				destMethod.invoke(dest, srcValue);
			} catch (Exception ex) {
				continue;
			}
		}
	}
   
}
