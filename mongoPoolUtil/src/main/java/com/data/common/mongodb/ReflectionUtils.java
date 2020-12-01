package com.data.common.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;

public class ReflectionUtils {

	private static Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

	/**
	 * 璋冪敤Getter鏂规硶.
	 */
	public static Object invokeGetterMethod(Object obj, String propertyName) {
		String getterMethodName = "get" + StringUtils.capitalize(propertyName);
		return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
	}

	/**
	 * 璋冪敤Setter鏂规硶.浣跨敤value鐨凜lass鏉ユ煡鎵維etter鏂规硶.
	 */
	public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
		invokeSetterMethod(obj, propertyName, value, null);
	}

	/**
	 * 璋冪敤Setter鏂规硶.
	 * 
	 * @param propertyType
	 *            鐢ㄤ簬鏌ユ壘Setter鏂规硶,涓虹┖鏃朵娇鐢╲alue鐨凜lass鏇夸唬.
	 */
	public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
		Class<?> type = propertyType != null ? propertyType : value.getClass();
		String setterMethodName = "set" + StringUtils.capitalize(propertyName);
		invokeMethod(obj, setterMethodName, new Class[] { type }, new Object[] { value });
	}

	/**
	 * 鐩存帴璇诲彇瀵硅薄灞炴?у??, 鏃犺private/protected淇グ绗?, 涓嶇粡杩噂etter鍑芥暟.
	 */
	public static Object getFieldValue(final Object obj, final String fieldName) {
		Field field = getAccessibleField(obj, fieldName);

		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
		}

		Object result = null;
		try {
			result = field.get(obj);
		} catch (IllegalAccessException e) {
			logger.error("涓嶅彲鑳芥姏鍑虹殑寮傚父{}", e.getMessage());
		}
		return result;
	}

	/**
	 * 鐩存帴璁剧疆瀵硅薄灞炴?у??, 鏃犺private/protected淇グ绗?, 涓嶇粡杩噑etter鍑芥暟.
	 */
	public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
		Field field = getAccessibleField(obj, fieldName);

		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
		}

		try {
			field.set(obj, value);
		} catch (IllegalAccessException e) {
			logger.error("涓嶅彲鑳芥姏鍑虹殑寮傚父:{}", e.getMessage());
		}
	}

	/**
	 * 寰幆鍚戜笂杞瀷, 鑾峰彇瀵硅薄鐨凞eclaredField, 骞跺己鍒惰缃负鍙闂?.
	 * 
	 * 濡傚悜涓婅浆鍨嬪埌Object浠嶆棤娉曟壘鍒?, 杩斿洖null.
	 */
	public static Field getAccessibleField(final Object obj, final String fieldName) {
		Assert.notNull(obj, "object涓嶈兘涓虹┖");
		Assert.hasText(fieldName, "fieldName");
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException e) {// NOSONAR
				// Field涓嶅湪褰撳墠绫诲畾涔?,缁х画鍚戜笂杞瀷
			}
		}
		return null;
	}

	/**
	 * 鐩存帴璋冪敤瀵硅薄鏂规硶, 鏃犺private/protected淇グ绗?. 鐢ㄤ簬涓?娆℃?ц皟鐢ㄧ殑鎯呭喌.
	 */
	public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
			final Object[] args) {
		Method method = getAccessibleMethod(obj, methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
		}

		try {
			return method.invoke(obj, args);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	/**
	 * 寰幆鍚戜笂杞瀷, 鑾峰彇瀵硅薄鐨凞eclaredMethod,骞跺己鍒惰缃负鍙闂?. 濡傚悜涓婅浆鍨嬪埌Object浠嶆棤娉曟壘鍒?, 杩斿洖null.
	 * 
	 * 鐢ㄤ簬鏂规硶闇?瑕佽澶氭璋冪敤鐨勬儏鍐?. 鍏堜娇鐢ㄦ湰鍑芥暟鍏堝彇寰桵ethod,鐒跺悗璋冪敤Method.invoke(Object obj, Object...
	 * args)
	 */
	public static Method getAccessibleMethod(final Object obj, final String methodName,
			final Class<?>... parameterTypes) {
		Assert.notNull(obj, "object涓嶈兘涓虹┖");

		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				Method method = superClass.getDeclaredMethod(methodName, parameterTypes);

				method.setAccessible(true);

				return method;

			} catch (NoSuchMethodException e) {// NOSONAR
				// Method涓嶅湪褰撳墠绫诲畾涔?,缁х画鍚戜笂杞瀷
			}
		}
		return null;
	}

	/**
	 * 閫氳繃鍙嶅皠, 鑾峰緱Class瀹氫箟涓０鏄庣殑鐖剁被鐨勬硾鍨嬪弬鏁扮殑绫诲瀷. 濡傛棤娉曟壘鍒?, 杩斿洖Object.class. eg. public UserDao
	 * extends HibernateDao<User>
	 * 
	 * @param clazz
	 *            The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be
	 *         determined
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Class<T> getSuperClassGenricType(final Class clazz) {
		return getSuperClassGenricType(clazz, 0);
	}

	/**
	 * 閫氳繃鍙嶅皠, 鑾峰緱Class瀹氫箟涓０鏄庣殑鐖剁被鐨勬硾鍨嬪弬鏁扮殑绫诲瀷. 濡傛棤娉曟壘鍒?, 杩斿洖Object.class.
	 * 
	 * 濡俻ublic UserDao extends HibernateDao<User,Long>
	 * 
	 * @param clazz
	 *            clazz The class to introspect
	 * @param index
	 *            the Index of the generic ddeclaration,start from 0.
	 * @return the index generic declaration, or Object.class if cannot be
	 *         determined
	 */
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}

	/**
	 * 灏嗗弽灏勬椂鐨刢hecked exception杞崲涓簎nchecked exception.
	 */
	public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
				|| e instanceof NoSuchMethodException) {
			return new IllegalArgumentException("Reflection Exception.", e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new RuntimeException("Unexpected Checked Exception.", e);
	}

	/**
	 * 鏍规嵁瀵硅薄鑾峰緱mongodb Update璇彞 闄d瀛楁浠ュ锛屾墍鏈夎璧嬪?肩殑瀛楁閮戒細鎴愪负淇敼椤?
	 */
	public static Update getUpdateObj(final Object obj) {
		if (obj == null)
			return null;
		Field[] fields = obj.getClass().getDeclaredFields();
		Update update = null;
		boolean isFirst = true;
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				Object value = field.get(obj);
				if (value != null) {
					if ("id".equals(field.getName().toLowerCase())
							|| "serialversionuid".equals(field.getName().toLowerCase()))
						continue;
					if (isFirst) {
						update = Update.update(field.getName(), value);
						isFirst = false;
					} else {
						update = update.set(field.getName(), value);
					}
				}

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return update;
	}
}
