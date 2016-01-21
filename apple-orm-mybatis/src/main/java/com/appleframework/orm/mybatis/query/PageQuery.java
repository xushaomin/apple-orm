package com.appleframework.orm.mybatis.query;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appleframework.model.Operator;
import com.appleframework.model.page.Pagination;
import com.appleframework.model.utils.TypeCaseHelper;

/**
 * 封装查询蚕食和查询条件
 * 
 * @author cruise.xu
 * 
 */
public class PageQuery extends HashMap<String, Object> implements Query {
	
	private static final long serialVersionUID = -244289359960417314L;

	/**
	 * 以BigDecimal类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return BigDecimal 键值
	 */
	public BigDecimal getAsBigDecimal(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "BigDecimal", null);
		if (obj != null)
			return (BigDecimal) obj;
		else
			return null;
	}

	/**
	 * 以Date类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return Date 键值
	 */
	public Date getAsDate(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "Date", "yyyy-MM-dd");
		if (obj != null)
			return (Date) obj;
		else
			return null;
	}

	/**
	 * 以Integer类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return Integer 键值
	 */
	public Integer getAsInteger(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "Integer", null);
		if (obj != null)
			return (Integer) obj;
		else
			return null;
	}

	/**
	 * 以Long类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return Long 键值
	 */
	public Long getAsLong(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "Long", null);
		if (obj != null)
			return (Long) obj;
		else
			return null;
	}
	
	/**
	 * 以Boolean类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return Boolean 键值
	 */
	public Boolean getAsBoolean(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "Boolean", null);
		if (obj != null)
			return (Boolean) obj;
		else
			return null;
	}

	/**
	 * 以String类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return String 键值
	 */
	public String getAsString(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "String", null);
		if (obj != null)
			return (String) obj;
		else
			return "";
	}

	/**
	 * 以Timestamp类型返回键值
	 * 
	 * @param key
	 *            键名
	 * @return Timestamp 键值
	 */
	public Timestamp getAsTimestamp(String key) {
		Object obj = TypeCaseHelper.convert(get(key), "Timestamp",
				"yyyy-MM-dd HH:mm:ss");
		if (obj != null)
			return (Timestamp) obj;
		else
			return null;
	}

	/**
	 * 给Dto压入第一个默认List对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param pList
	 *            压入Dto的List对象
	 */
	public void setDefaultAList(List<?> pList) {
		put("defaultAList", pList);
	}

	/**
	 * 给Dto压入第二个默认List对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param pList
	 *            压入Dto的List对象
	 */
	public void setDefaultBList(List<?> pList) {
		put("defaultBList", pList);
	}

	/**
	 * 获取第一个默认List对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param pList
	 *            压入Dto的List对象
	 */
	public List<?> getDefaultAList() {
		return (List<?>) get("defaultAList");
	}

	/**
	 * 获取第二个默认List对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param pList
	 *            压入Dto的List对象
	 */
	public List<?> getDefaultBList() {
		return (List<?>) get("defaultBList");
	}

	/**
	 * 给Dto压入第一个默认PO对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param PO
	 *            压入Dto的PO对象
	 */
	public void setDefaultPo(Object po) {
		put("defaultPo", po);
	}

	/**
	 * 获取第一个默认PO对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param PO
	 *            压入Dto的PO对象
	 */
	public Object getDefaultPo() {
		return get("defaultPo");
	}

	/**
	 * 给Pagination压入第一个默认Pagination对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param Pagination
	 *            压入Dto的Pagination对象
	 */
	public void setDefaultPage(Pagination page) {
		put("defaultPage", page);
	}

	/**
	 * 获取第一个默认Pagination对象<br>
	 * 为了方便存取(省去根据Key来存取和类型转换的过程)
	 * 
	 * @param Pagination
	 *            压入Pagination的PO对象
	 */
	public Pagination getDefaultPage() {
		return (Pagination) get("defaultPage");
	}

	@Override
	public void setDefaultOperater(Operator operater) {
		put("defaultOperater", operater);
	}

	@Override
	public Operator getDefaultOperater() {
		return (Operator) get("defaultOperater");
	}

	@Override
	public void setDefaultIds(String ids) {
		put("defaultIds", ids);
	}

	@Override
	public String getDefaultIds() {
		return (String) get("defaultIds");
	}

	@Override
	public void setDefaultId(Object id) {
		put("defaultId", id);
	}

	@Override
	public Object getDefaultId() {
		return get("defaultId");
	}
	
	
	
	
	/**
	 * 给定一个分页对象，创建查询对象<br>
	 * 
	 * @param page
	 *            QueryPage
	 */
	public static PageQuery create(Pagination page) {
		PageQuery query = new PageQuery();
		query.setDefaultPage(page);
		return query;
	}
	
	public static PageQuery create() {
		PageQuery query = new PageQuery();
		return query;
	}
	
	public static PageQuery create(Pagination page, Map<String, Object> params) {
		PageQuery query = new PageQuery();
		query.setDefaultPage(page);
		query.putAll(params);
		return query;
	}
	
	public static PageQuery create(Object... pairs) {
		PageQuery query = new PageQuery();
		query.addParameters(pairs);
		return query;
	}
	
	public void addParameters(Object... pairs) {
        if (pairs == null || pairs.length == 0) {
            return;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i ++) {
            map.put((String)pairs[2 * i], pairs[2 * i + 1]);
        }
        putAll(map);
    }
	
	/**
	 * Add parameters to a new url.
	 * 
	 * @param parameters
	 * @return A new URL 
	 */
    public void addParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return;
        }
        putAll(parameters);
    }
    
    
    
    @Override
	public void addParameters(String key, Object value) {
		put(key, value);
	}

	public Object getParameter(String key) {
        return get(key);
    }

    public String getParameter(String key, String defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return String.valueOf(value);
    }
    
    public double getParameter(String key, double defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Double)value;
    }
    
    public float getParameter(String key, float defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Float)value;
    }

    public long getParameter(String key, long defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Long)value;
    }

    public int getParameter(String key, int defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Integer)value;
    }

    public short getParameter(String key, short defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Short)value;
    }

    public byte getParameter(String key, byte defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Byte)value;
    }
    
    public boolean getParameter(String key, boolean defaultValue) {
        Object value = getParameter(key);
        if (value == null) {
            return defaultValue;
        }
        return (Boolean)value;
    }

	
}