package com.appleframework.orm.hibernate.client;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

import com.appleframework.model.page.Pagination;
import com.appleframework.orm.hibernate.model.BaseEntity;
import com.appleframework.orm.hibernate.types.Condition;
import com.appleframework.orm.hibernate.types.Finder;
import com.appleframework.orm.hibernate.types.Nullable;
import com.appleframework.orm.hibernate.types.OrderBy;
import com.appleframework.orm.hibernate.types.Updater;
import com.appleframework.orm.hibernate.utils.BeanUtility;
import com.appleframework.orm.hibernate.utils.ReflectionUtility;

/**
 * DAO鍩虹被銆�
 * 
 * 鎻愪緵hql鍒嗛〉鏌ヨ锛宔xample鍒嗛〉鏌ヨ锛屾嫹璐濇洿鏂扮瓑鍔熻兘銆�
 * 
 * 
 * @param <T>
 */
public class HibernateBaseDAO2 {
	
	private static Logger logger = Logger.getLogger(HibernateBaseDAO2.class);
	
	private SessionFactory sessionFactory;
	

	@Resource
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public Session getSession() {
		Session	session = sessionFactory.getCurrentSession();
		return session;
	}

	/**
	 * 鏂板涓�鏉℃暟鎹簱璁板綍,杩斿洖涓婚敭
	 * 
	 * @param entity
	 *@return
	 */
	public Serializable save(Object entity) throws HibernateException {
		Assert.notNull(entity);
		try {
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.CREATE_TIME_PROPERTY_NAME, new Date());
		} catch (Exception e) {}
		return getSession().save(entity);
	}
	
	/**
	 * 鍒锋柊鏁翠釜Session缂撳瓨
	 * 
	 *@return
	 */
	public void flush() throws HibernateException {
		getSession().flush();
	}

	/**
	 * 鏇存柊涓�鏉¤褰�
	 * 
	 * @param entity
	 *@return
	 *@throws HibernateException
	 * 
	 */
	public void update(Object entity) throws HibernateException {
		Assert.notNull(entity);
		try {
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.UPDATE_TIME_PROPERTY_NAME, new Date());
		} catch (Exception e) {}
		getSession().update(entity);
	}

	/**
	 * 浣跨敤update pojo set aa=xx 璇彞鎵归噺鏇存柊,濡傛灉hql璇彞甯︽湁鍙傛暟锛岄偅涔坧aram鍙傛暟涓嶄负绌� 渚嬪瓙 update pojo
	 * set name=:name,address=:add where id=:id ,param涓湁3涓厓绱狅紝1 key ="name"
	 * value="鍙傛暟鍊�" 2 key="add" value="鍙傛暟鐨勫��" 3 key="id" value="鍙傛暟鐨勫��";
	 * 濡傛灉hql涓病鏈夊弬鏁帮紝param瀵硅薄缃负NULL 2010-1-22 
	 * 
	 * @param hql
	 * @param param
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	public void update(String updatehql, Map param) {
		Session session = getSession();
		Query query = session.createQuery(updatehql);
		if (param != null && !param.isEmpty())
			setFilterToQuery(query,param);
		query.executeUpdate();
		session.flush();
	}

	/**
	 * 鏇存柊鎴栬�呮柊澧炰竴鏉¤褰曪紝濡傛灉鏈夋柊ID灏辨槸鏂板锛屽惁鍒欏氨鏄洿鏂般��
	 * 
	 * @param entity
	 *@throws HibernateException
	 * 
	 */
	public void saveOrUpdate(Object entity) throws HibernateException {
		Assert.notNull(entity);
		try {
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.CREATE_TIME_PROPERTY_NAME, new Date());
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.UPDATE_TIME_PROPERTY_NAME, new Date());
		} catch (Exception e) {}
		getSession().saveOrUpdate(entity);
	}

	/**
	 * Description:鏂板鎴栬�呬繚瀛樺涓� hibernate pojo
	 * 
	 * @param objList
	 *            hibernate pojo
	 * @throws HibernateException
	 */
	public void saveOrUpdate(Collection<Object> objList) throws HibernateException {
		if (objList == null) {
			logger.error("The Object to save is null!");
			throw new HibernateException(this.getClass() + ": The objList to saveOrUpdate is null!");
		}
		for (Object object : objList) {
			try {
				ReflectionUtility.invokeSetterMethod(object, BaseEntity.CREATE_TIME_PROPERTY_NAME, new Date());
				ReflectionUtility.invokeSetterMethod(object, BaseEntity.CREATE_TIME_PROPERTY_NAME, new Date());
			} catch (Exception e) {}
			getSession().saveOrUpdate(object);
		}		
	}

	/**
	 * 
	 *@param entity
	 *@return
	 */
	public Object merge(Object entity) throws HibernateException {
		Assert.notNull(entity);
		try {
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.UPDATE_TIME_PROPERTY_NAME, new Date());
		} catch (Exception e) {}
		return getSession().merge(entity);
	}

	/**
	 * 鏍规嵁pojo瀵硅薄鍒犻櫎鏁版嵁
	 * 
	 * @param entity
	 * 
	 */
	public void delete(Object entity) throws HibernateException {
		Assert.notNull(entity);
		getSession().delete(entity);
	}

	/**
	 *浣跨敤delete from pojo 璇彞鎵归噺鍒犻櫎,濡傛灉hql璇彞甯︽湁鍙傛暟锛岄偅涔坧aram鍙傛暟涓嶄负绌� 渚嬪瓙 delete from pojo
	 * where name=:name and age>:age1 and age<:age2 ,param涓湁3涓厓绱狅紝1 key ="name"
	 * value="鍙傛暟鍊�" 2 key="age1" value="鍙傛暟鐨勫��" 3 key="age2" value="鍙傛暟鐨勫��";
	 * 濡傛灉hql涓病鏈夊弬鏁帮紝param瀵硅薄缃负NULL 2010-1-22 
	 * 
	 * @param hql
	 *@param param
	 */
	@SuppressWarnings("rawtypes")
	public void delete(String hql, Map param) throws HibernateException {
		Session session = getSession();
		Query query = session.createQuery(hql);
		if (param != null && !param.isEmpty())
			setFilterToQuery(query, param);
		//query.setProperties(param);
		query.executeUpdate();
		session.flush();
	}

	/**
	 * 鎵归噺鍒犻櫎POJO List
	 * 
	 * @param ObjectList
	 */
	public void delete(List<Object> ObjectList) throws HibernateException {
		if (ObjectList != null && ObjectList.size() > 0) {
			logger.info("delete method begin");
			for (Object object : ObjectList) {
				getSession().delete(object);
			}
			logger.info("delete method end");
		}
	}

	/**
	 * Description:瑁呰浇鎸囧畾ID鐨刪ibernate pojo
	 * 
	 * @param entityClass
	 *            pojo绫诲悕
	 * @param id
	 *            pojo鐨刬d
	 * @return object entityClass瀵瑰簲鐨刾ojo
	 * @throws HibernateException
	 */
	public Object load(Class<?> entityClass, Serializable id) throws HibernateException {
		Assert.notNull(id);
		return getSession().get(entityClass, id);
	}
	
	/**
	* 鏍规嵁hql璇彞鏌ヨ鏁版嵁锛岃繑鍥炵殑缁撴灉闆唋ist 涓簃ap鍏冪礌闆嗗悎銆俬ql 涓绱㈠瓧娈电殑瀛楁涓�瀹氳鍒版湁 as 鍒悕锛宮ap涓璳ey鐨勫�煎氨鏄� 瀛楁鐨� as鍒悕锛�
	* value鍊煎氨鏄瓧娈电殑鍊笺�俻aram 涓篽ql璇彞涓弬鏁板�硷紝濡傛灉hql涓甫鏈夊弬鏁帮紝param涓嶄负绌� 渚嬪锛�
	* selec field1 as a 锛宖ield2 as b from Ttable where field3 =:asdf,param涓氨鏈変竴涓厓绱狅紝key=asdf,value=鍙傛暟鍊硷紝
	* 濡傛灉hql涓病鏈夊弬鏁帮紝param缃负NULL銆�
	* 2010-1-28 
	* xusm
	*@param hql
	*@param param
	*@return
	*@throws HibernateException
	*/
	@SuppressWarnings("rawtypes")
	public Pagination getMapListByHql(String hql, Map param) throws HibernateException {
		return getMapListByHql(hql,-1,1,param);
	}
	
	/**
	 * 鏍规嵁hql璇彞鏌ヨ鏁版嵁锛岃繑鍥炵殑缁撴灉闆唋ist 涓簃ap鍏冪礌闆嗗悎銆俬ql 涓绱㈠瓧娈电殑瀛楁涓�瀹氳鍒版湁 as 鍒悕锛宮ap涓璳ey鐨勫�煎氨鏄� 瀛楁鐨� as鍒悕锛�
	 * value鍊煎氨鏄瓧娈电殑鍊笺�俻aram 涓篽ql璇彞涓弬鏁板�硷紝濡傛灉hql涓甫鏈夊弬鏁帮紝param涓嶄负绌� 渚嬪锛�
	 * selec field1 as a 锛宖ield2 as b from Ttable where field3 =:asdf,param涓氨鏈変竴涓厓绱狅紝key=asdf,value=鍙傛暟鍊硷紝
	 * 濡傛灉hql涓病鏈夊弬鏁帮紝param缃负NULL銆�
	 * 
	 * 褰搕argetPage=-1鏃� 琛ㄧず涓嶅垎椤点��	
	 * 2010-1-28 
	 * xusm
	 *@param hql
	 *@param targetPage
	 *@param pagesize
	 *@param param
	 *@return
	 *@throws HibernateException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pagination getMapListByHql(String hql, int targetPage, int pagesize, Map param) throws HibernateException {
		logger.info("******HibernateBaseDao getMapListByHql method begin******");
		Session session = getSession();
		if (((targetPage <= 0) && (targetPage != -1))) {
			List list = new ArrayList();
			Pagination p = new Pagination(0, 0, 0);
			p.setList(list);
			return p;
		}

		Finder finder = new Finder(hql);
		int totalCount = countQueryResult(session, finder, param);
		if (pagesize == -1)
			pagesize = totalCount;
		Pagination p = new Pagination(targetPage, pagesize, totalCount);
		if (totalCount < 1 || ((targetPage <= 0 && targetPage != -1) || pagesize <= 0)) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = session.createQuery(finder.getOrigHql());
		if (param != null && !param.isEmpty()) {
			setFilterToQuery(query, param);
			// queryObject.setProperties(param);
		}
		// finder.setParamsToQuery(query);

		if (targetPage != -1) {// 绛変簬-1鏃讹紝涓嶄綔鍒嗛〉
			query.setFirstResult((int)p.getFirstResult());
			query.setMaxResults((int)p.getPageSize());
		}
		query.setCacheable(true);// set true
		String[] name = query.getReturnAliases();
		// Type type[] = query.getReturnTypes();
		List returnList = new ArrayList();
		List list = query.list();
		if (list == null || list.size() == 0) {
			p.setList(returnList);
			return p;
		}
		if (name != null && name.length > 1 && list.get(0) instanceof Object[]) {// 瀵硅薄鏁扮粍
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Object[] data = (Object[]) iter.next();
				HashMap map = new HashMap();
				for (int i = 0; i < name.length; i++) {
					map.put(name[i], data[i]);
				}
				returnList.add(map);
			}
		} else if (list.get(0) instanceof Object) {// pojo
			String keyArray[] = getKeyArray(list.get(0));
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Object bean = (Object) iter.next();
				HashMap map = new HashMap();
				for (int i = 0; i < keyArray.length; i++) {
					if (!"class".equals(keyArray[i])) {
						try {
							map.put(keyArray[i], BeanUtilsBean.getInstance().getPropertyUtils().getSimpleProperty(bean, keyArray[i]));
						} catch (IllegalAccessException e) {
							logger.error(e);
						} catch (InvocationTargetException e) {
							logger.error(e);
						} catch (NoSuchMethodException e) {
							logger.error(e);
						}
					}
				}
				returnList.add(map);
			}
		} else {// 瀵硅薄
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Object data = (Object) iter.next();
				HashMap map = new HashMap();
				map.put(name[0], data);
				returnList.add(map);
			}
		}
		p.setList(returnList);
		logger.info("******HibernateBaseDao getMapListByHql method begin******");
		return p;
	}
	/**
	 * 浠ュ崌搴忔垨鑰呴檷搴忕殑瑙勫垯锛岃幏寰椾竴涓〃鐨勬墍鏈夎褰�
	 * 
	 * @param entityClass
	 *@param orders
	 *@return
	 *@throws HibernateException
	 */
	public Pagination findAll(Class<?> entityClass, OrderBy... orders) throws HibernateException {
		return findAll(entityClass, -1, 1, orders);
	}

	/**
	 * 鍗囧簭鎴栬�呴檷搴忕殑瑙勫垯锛岃幏寰椾竴涓〃鐨勫垎椤佃褰�
	 * 
	 * @param entityClass
	 *@param pageNo
	 *@param pageSize
	 *@param orders
	 *@return
	 *@throws HibernateException
	 */
	@SuppressWarnings("rawtypes")
	public Pagination findAll(Class<?> entityClass, int pageNo, int pageSize, OrderBy... orders) throws HibernateException {
		logger.info("******findAll method begin******");
		Session session = getSession();
		Criteria crit = createCriteria(session, entityClass);
		int totalCount = ((Number) crit.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		if (pageSize == -1)
			pageSize = totalCount;
		Pagination p = new Pagination(pageNo, pageSize, totalCount);
		List list = findByCriteria(crit, pageNo, pageSize, null, OrderBy.asOrders(orders));
		p.setList(list);
		logger.info("******findAll method begin******");
		return p;

	}

	/**
	 * 鎸塇QL鏌ヨ瀵硅薄鍒楄〃杩斿洖鐨勭粨鏋滈泦鏄疧bject鏁扮粍鐨凩ist.
	 * 
	 * @param hql
	 *            hql璇彞
	 * @param param
	 *            鍙傛暟鍚嶅�煎锛屽鏋滄病鏈夊弬鏁拌缃负null銆�
	 */
	@SuppressWarnings("rawtypes")
	public List find(String hql, Map param) throws HibernateException {
		logger.info("******find method begin******");
		Session session = getSession();
		Query query = createQuery(session, hql, param);
		logger.info("******find method end******");
		return query.list();
	}
	
	

	/**
	 * 鎸塇QL鏌ヨ瀵硅薄鍒楄〃杩斿洖鐨勭粨鏋滈泦鏄疧bject鏁扮粍鐨凩ist.
	 * 
	 * @param hql
	 *            hql璇彞
	 * @param param
	 *            鍙傛暟鍚嶅�煎锛屽鏋滄病鏈夊弬鏁拌缃负null銆�
	 */
	@SuppressWarnings("rawtypes")
	public List find(String hql, Map param, int size) throws HibernateException {
		logger.info("******find method begin******");
		Session session = getSession();
		Query query = createQuery(session, hql, param);
		if (size > -1) {
			query.setFirstResult(0);
			query.setMaxResults(size);
		}
		logger.info("******find method end******");
		return query.list();
	}
	
	/**
	 * 鎸塇QL鏌ヨ鍞竴瀵硅薄.
	 */
	@SuppressWarnings("rawtypes")
	public Object findUnique(String hql, Map param) throws HibernateException {
		logger.info("****** findUnique method begin******");
		Session session = getSession();
		Query query = createQuery(session, hql, param);
		logger.info("****** findUnique method begin******");
		return query.uniqueResult();
	}
		
	/**
	 * 鏍规嵁Finder 鏌ヨ鏁版嵁
	 * 
	 * @param finder
	 *@param pageNo
	 *@param pageSize
	 *@return
	 *@throws HibernateException
	 */
	@SuppressWarnings("rawtypes")
	public Object findOne(Finder finder) throws HibernateException {
		logger.info("******find method begin******");
		Session session = getSession();
		Object object = null;
		Query query = session.createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		query.setFirstResult(0);
		query.setMaxResults(1);
		List list = query.list();
		if (list.size() > 0) {
			object = list.get(0);
		} else {
			return null;
		}
		logger.info("******find method end******");
		return object;
	}
	
	
	/**
	 * 鎸塇QL鏌ヨ鍞竴瀵硅薄.
	 */
	@SuppressWarnings("rawtypes")
	public Object findOne(String hql, Map param) throws HibernateException {
		logger.info("****** findUnique method begin******");
		Session session = getSession();
		Object object = null;
		Query query = createQuery(session, hql, param);
		query.setFirstResult(0);
		query.setMaxResults(1);
		List list = query.list();
		if (list.size() > 0) {
			object = list.get(0);
		} else {
			return null;
		}
		return object;
	}

	/**
	 * 鎸夊睘鎬ф煡鎵惧璞″垪琛�.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Object> findByProperty(Class entityClass, String property, Object value) throws HibernateException {
		logger.info("******findByProperty method begin******");
		Assert.hasText(property);
		Session session = getSession();
		List list =createCriteria(session, entityClass, Restrictions.eq(property, value)).list(); 
		logger.info("******findByProperty method end******");
		return list;
	}

	/**
	 * 鎸夊睘鎬ф煡鎵惧敮涓�瀵硅薄.
	 */
	@SuppressWarnings("rawtypes")
	public Object findUniqueByProperty(Class entityClass, String property, Object value) throws HibernateException {
		logger.info("******findUniqueByProperty method begin******");
		Assert.hasText(property);
		Assert.notNull(value);
		Session session = getSession();
		Object obj = createCriteria(session, entityClass, Restrictions.eq(property, value)).uniqueResult();
		logger.info("******findUniqueByProperty method end******");
		return obj;
		
	}

	@SuppressWarnings("rawtypes")
	public int countByProperty(Class entityClass, String property, Object value) throws HibernateException {
		logger.info("******countByProperty method begin******");
		Assert.hasText(property);
		Assert.notNull(value);
		Session session = getSession();
		int count = ((Number) (createCriteria(session, entityClass, Restrictions.eq(property, value)).setProjection(Projections.rowCount()).uniqueResult())).intValue();
		logger.info("******countByProperty method end******");
		return count;
	}

	/**
	 * 鏍规嵁Finder 鍒嗛〉鏌ヨ鏁版嵁
	 * 
	 * @param finder
	 *@param pageNo
	 *@param pageSize
	 *@return
	 *@throws HibernateException
	 */
	@SuppressWarnings("rawtypes")
	public Pagination find(Finder finder, int pageNo, int pageSize) throws HibernateException {
		logger.info("******find method begin******");
		Session session = getSession();
		Pagination p;
		int totalCount = countQueryResult(session, finder, null);
		if (pageSize == -1)
			pageSize = totalCount;
		p = new Pagination(pageNo, pageSize, totalCount);
		if (totalCount < 1 || ((pageNo <= 0 && pageNo != -1) || pageSize <= 0)) {
			p.setList(new ArrayList());
			return p;
		}
		Query query = session.createQuery(finder.getOrigHql());
		finder.setParamsToQuery(query);
		if (pageNo != -1) {
			query.setFirstResult((int)p.getFirstResult());
			query.setMaxResults((int)p.getPageSize());
		}
		List list = query.list();
		p.setList(list);

		logger.info("******find method end******");
		return p;
	}

	/**
	 * 鏍规嵁Finder鑾峰緱鏁版嵁
	 * 
	 * @param finder
	 *@return
	 *@throws HibernateException
	 */
	public Pagination find(Finder finder) throws HibernateException {
		return find(finder, -1, -1);
	}

	/**
	 * 鏍规嵁Conds he exclude鏉′欢鑾峰緱 缁撴灉
	 * 
	 * @param eg
	 *@param anyWhere
	 *@param conds
	 *@param exclude
	 *@return
	 *@throws HibernateException
	 */
	public Pagination findByEgList(Object eg, boolean anyWhere, Condition[] conds, String... exclude) throws HibernateException {
		return findByEg(eg, anyWhere, conds, -1, 1, exclude);
	}

	@SuppressWarnings("rawtypes")
	public Pagination findByEg(Object eg, boolean anyWhere, Condition[] conds, int page, int pageSize, String... exclude) 
			throws HibernateException {
		logger.info("******findByEg method begin ******");
		Session session = getSession();
		Order[] orderArr = null;
		Condition[] condArr = null;
		if (conds != null && conds.length > 0) {
			List<Order> orderList = new ArrayList<Order>();
			List<Condition> condList = new ArrayList<Condition>();
			for (Condition c : conds) {
				if (c instanceof OrderBy) {
					orderList.add(((OrderBy) c).getOrder());
				} else {
					condList.add(c);
				}
			}
			orderArr = new Order[orderList.size()];
			condArr = new Condition[condList.size()];
			orderArr = orderList.toArray(orderArr);
			condArr = condList.toArray(condArr);
		}
		Criteria crit = getCritByEg(session, eg, anyWhere, condArr, exclude);
		int totalCount = ((Number) crit.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		if (pageSize <= 0)
			pageSize = totalCount;
		Pagination p = new Pagination(page, pageSize, totalCount);
		List list = findByCriteria(crit, page, pageSize, null, orderArr);
		p.setList(list);
		logger.info("******findByEg method end ******");
		return p;
	}

	public void refresh(Object entity) throws HibernateException {
		getSession().refresh(entity);
	}

	public Object updateDefault(Object entity) {
		try {
			ReflectionUtility.invokeSetterMethod(entity, BaseEntity.CREATE_TIME_PROPERTY_NAME, new Date());
		} catch (Exception e) {}
		return updateByUpdater(Updater.create(entity));
	}

	@SuppressWarnings("deprecation")
	private Object updateByUpdater(Updater updater) throws HibernateException {
		ClassMetadata cm = getCmd(updater.getBean().getClass());
		if (cm == null) {
			throw new HibernateException("鎵�鏇存柊鐨勫璞℃病鏈夋槧灏勬垨涓嶆槸瀹炰綋瀵硅薄");
		}
		Object bean = updater.getBean();
		Session session = getSession();
		Object po = session.load(bean.getClass(), cm.getIdentifier(bean));
		updaterCopyToPersistentObject(updater, po);
		return po;
	}

	/**
	 * 鏍规嵁鏌ヨ鍑芥暟涓庡弬鏁板垪琛ㄥ垱寤篞uery瀵硅薄,鍚庣画鍙繘琛屾洿澶氬鐞�,杈呭姪鍑芥暟.
	 */
	@SuppressWarnings("rawtypes")
	private Query createQuery(Session session, String queryString, Map param) {
		Assert.hasText(queryString);
		Query queryObject = session.createQuery(queryString);
		if (param != null && !param.isEmpty()) {
			setFilterToQuery(queryObject,param);
			//queryObject.setProperties(param);
		}
		return queryObject;
	}

	@SuppressWarnings("rawtypes")
	private List findByCriteria(Criteria crit, int pageNo, int pageSize, Projection projection, Order... orders) {
		if ((pageNo <= 0 && pageNo != -1) || pageSize < 1) {
			return new ArrayList();
		}
		crit.setProjection(projection);
		if (projection == null) {
			crit.setResultTransformer(Criteria.ROOT_ENTITY);
		}
		if (orders != null) {
			for (Order order : orders) {
				crit.addOrder(order);
			}
		}
		if (pageNo != -1) {
			crit.setFirstResult((pageNo - 1) * pageSize);
			crit.setMaxResults(pageSize);
		}
		return crit.list();
	}

	/**
	 * 閫氳繃count鏌ヨ鑾峰緱鏈鏌ヨ鎵�鑳借幏寰楃殑瀵硅薄鎬绘暟.
	 * 
	 * @param finder
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private int countQueryResult(Session session, Finder finder, Map param) {
		int returnInt = 0;
		Query query = session.createQuery(finder.getRowCountHql());
		if (param != null && !param.isEmpty()) {
			setFilterToQuery(query,param);
			//queryObject.setProperties(param);
		}
		finder.setParamsToQuery(query);
		try {
			Iterator<?> it = query.iterate();
			if(it.hasNext()){
				returnInt = ((Number) it.next()).intValue();
			}
			else{
				returnInt = 0;
			}
		}
		catch(Exception ex){
			logger.error(ex.getMessage());
		}
		return returnInt;
	}
	
	
	/**
	 * 閫氳繃count鏌ヨ鑾峰緱鏈鏌ヨ鎵�鑳借幏寰楃殑瀵硅薄鎬绘暟.
	 * 
	 * @param finder
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public int countQueryResult(Finder finder, Map param) {
		Session session = getSession();
		int returnInt = 0;
		Query query = session.createQuery(finder.getRowCountHql());
		if (param != null && !param.isEmpty()) {
			setFilterToQuery(query,param);
			//queryObject.setProperties(param);
		}
		finder.setParamsToQuery(query);
		Iterator it = query.iterate();
		if(it.hasNext()){
			returnInt = ((Number) it.next()).intValue();
		}
		else{
			returnInt = 0;
		}
		return returnInt;
	}

	@SuppressWarnings("deprecation")
	private Criteria getCritByEg(Session session, Object bean, boolean anyWhere, Condition[] conds, String... exclude) {
		Criteria crit = session.createCriteria(bean.getClass());
		Example example = Example.create(bean);
		example.setPropertySelector(NOT_BLANK);
		if (anyWhere) {
			example.enableLike(MatchMode.ANYWHERE);
			example.ignoreCase();
		}
		for (String p : exclude) {
			example.excludeProperty(p);
		}
		crit.add(example);
		// 澶勭悊鎺掑簭鍜宨s null瀛楁
		if (conds != null) {
			for (Condition o : conds) {
				if (o instanceof OrderBy) {
					OrderBy order = (OrderBy) o;
					crit.addOrder(order.getOrder());
				} else if (o instanceof Nullable) {
					Nullable isNull = (Nullable) o;
					if (isNull.isNull()) {
						crit.add(Restrictions.isNull(isNull.getField()));
					} else {
						crit.add(Restrictions.isNotNull(isNull.getField()));
					}
				} else {
					// never
				}
			}
		}
		// 澶勭悊many to one鏌ヨ
		ClassMetadata cm = getCmd(bean.getClass());
		String[] fieldNames = cm.getPropertyNames();
		for (String field : fieldNames) {
			Object o = cm.getPropertyValue(bean, field);
			if (o == null) {
				continue;
			}
			ClassMetadata subCm = getCmd(o.getClass());
			if (subCm == null) {
				continue;
			}
			Serializable id = subCm.getIdentifier(o);
			if (id != null) {
				Serializable idName = subCm.getIdentifierPropertyName();
				crit.add(Restrictions.eq(field + "." + idName, id));
			} else {
				crit.createCriteria(field).add(Example.create(o));
			}
		}
		return crit;
	}

	/**
	 * 灏嗘洿鏂板璞℃嫹璐濊嚦瀹炰綋瀵硅薄锛屽苟澶勭悊many-to-one鐨勬洿鏂般��
	 * 
	 * @param updater
	 * @param po
	 */
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	private void updaterCopyToPersistentObject(Updater updater, Object po) {
		Map map = BeanUtility.describe(updater.getBean());
		Set<Map.Entry<String, Object>> set = map.entrySet();
		for (Map.Entry<String, Object> entry : set) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if (!updater.isUpdate(name, value)) {
				continue;
			}
			if (value != null) {
				Class<?> valueClass = value.getClass();
				ClassMetadata cm = getCmd(valueClass);
				if (cm != null) {
					Serializable vid = cm.getIdentifier(value);
					// 濡傛灉鏇存柊鐨刴any to one鐨勫璞＄殑id涓虹┖锛屽垯灏唌any to one璁剧疆涓簄ull銆�
					if (vid != null) {
						value = getSession().load(valueClass, vid);
					} else {
						value = null;
					}
				}
			}
			try {
				PropertyUtils.setProperty(po, name, value);
			} catch (Exception e) {
				// never
				logger.warn("鏇存柊瀵硅薄鏃讹紝鎷疯礉灞炴�у紓甯�", e);
			}
		}
	}

	/**
	 * 鏍规嵁Criterion鏉′欢鍒涘缓Criteria,鍚庣画鍙繘琛屾洿澶氬鐞�,杈呭姪鍑芥暟.
	 */
	@SuppressWarnings("rawtypes")
	private Criteria createCriteria(Session session, Class entityClass, Criterion... criterions) {
		Criteria criteria = null;
		try {
			criteria = session.createCriteria(entityClass);
			for (Criterion c : criterions) {
				criteria.add(c);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return criteria;
	}

	@SuppressWarnings("rawtypes")
	private static void setFilterToQuery(Query query, Map filterMap) throws HibernateException {
		if (filterMap == null)
			return;
		Set entries = filterMap.entrySet();
		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			query.setParameter(key.toString(), value);
		}
	}
	/**
	 * 鍙栧緱pojo涓墍鏈夋垚鍛樺彉閲忓悕
	 *
	 * @param beanClass
	 *            pojo
	 * @return
	 */
	@SuppressWarnings({ "deprecation", "rawtypes" })
	private String[] getKeyArray(Object beanClass) {
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

	private ClassMetadata getCmd(Class<?> clazz) {
		return (ClassMetadata) sessionFactory.getClassMetadata(clazz);
	}

	public static final NotBlankPropertySelector NOT_BLANK = new NotBlankPropertySelector();

	/**
	 * 涓嶄负绌虹殑EXAMPLE灞炴�ч�夋嫨鏂瑰紡
	 * 
	 * @author xusm
	 * 
	 */
	static final class NotBlankPropertySelector implements PropertySelector {
		private static final long	serialVersionUID	= 1L;
		public boolean include(Object object, String property, Type type) {
			return object != null && !(object instanceof String && StringUtils.isBlank((String) object));
		}
	}
}
