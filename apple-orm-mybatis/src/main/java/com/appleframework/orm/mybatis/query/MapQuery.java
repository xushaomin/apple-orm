package com.appleframework.orm.mybatis.query;

import java.util.HashMap;
import java.util.Map;

import com.appleframework.model.Operator;
import com.appleframework.model.page.Pagination;

/**
 * 封装查询蚕食和查询条件
 * 
 * @author cruise.xu
 * 
 */
public class MapQuery extends HashMap<String, Object> implements Query  {
	
	private static final long serialVersionUID = -8249193845207112212L;

	@Override
	public void setDefaultPo(Object po) {
		this.put("defaultPo", po);
	}

	@Override
	public Object getDefaultPo() {
		return get("defaultPo");
	}

	@Override
	public void setDefaultPage(Pagination page) {
		this.put("page", page);
	}

	@Override
	public void setDefaultOperater(Operator operater) {
		this.put("operater", operater);
	}

	@Override
	public void setDefaultIds(String ids) {
		this.put("ids", ids);
	}

	@Override
	public void setDefaultId(Object id) {
		this.put("id", id);
	}

	@Override
	public String getDefaultIds() {
		return (String) get("defaultIds");
	}

	@Override
	public Operator getDefaultOperater() {
		return (Operator) get("defaultOperater");
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
	public Object getDefaultId() {
		return get("defaultId");
	}
	
	/**
	 * 给定一个分页对象，创建查询对象<br>
	 * 
	 * @param page
	 *            QueryPage
	 */
	
	public static MapQuery create() {
		MapQuery query = new MapQuery();
		return query;
	}
	
	public static MapQuery create(Map<String, Object> params) {
		MapQuery query = new MapQuery();
		query.putAll(params);
		return query;
	}
	
	public static MapQuery create(Object... pairs) {
		MapQuery query = new MapQuery();
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