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
 * DAO基类。
 * 
 * 提供hql分页查询，example分页查询，拷贝更新等功能。
 * 
 * 
 * @param <T>
 */
public class HibernateBaseDAO {
	
	private static Logger logger = Logger.getLogger(HibernateBaseDAO.class);
	
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
	 * 新增一条数据库记录,返回主键
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
	 * 刷新整个Session缓存
	 * 
	 *@return
	 */
	public void flush() throws HibernateException {
		getSession().flush();
	}

	/**
	 * 更新一条记录
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
	 * 使用update pojo set aa=xx 语句批量更新,如果hql语句带有参数，那么param参数不为空 例子 update pojo
	 * set name=:name,address=:add where id=:id ,param中有3个元素，1 key ="name"
	 * value="参数值" 2 key="add" value="参数的值" 3 key="id" value="参数的值";
	 * 如果hql中没有参数，param对象置为NULL 2010-1-22 
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
	 * 更新或者新增一条记录，如果有新ID就是新增，否则就是更新。
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
	 * Description:新增或者保存多个 hibernate pojo
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
	 * 根据pojo对象删除数据
	 * 
	 * @param entity
	 * 
	 */
	public void delete(Object entity) throws HibernateException {
		Assert.notNull(entity);
		getSession().delete(entity);
	}

	/**
	 *使用delete from pojo 语句批量删除,如果hql语句带有参数，那么param参数不为空 例子 delete from pojo
	 * where name=:name and age>:age1 and age<:age2 ,param中有3个元素，1 key ="name"
	 * value="参数值" 2 key="age1" value="参数的值" 3 key="age2" value="参数的值";
	 * 如果hql中没有参数，param对象置为NULL 2010-1-22 
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
	 * 批量删除POJO List
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
	 * Description:装载指定ID的hibernate pojo
	 * 
	 * @param entityClass
	 *            pojo类名
	 * @param id
	 *            pojo的id
	 * @return object entityClass对应的pojo
	 * @throws HibernateException
	 */
	public Object load(Class<?> entityClass, Serializable id) throws HibernateException {
		Assert.notNull(id);
		return getSession().get(entityClass, id);
	}
	
	/**
	* 根据hql语句查询数据，返回的结果集list 为map元素集合。hql 中检索字段的字段一定要到有 as 别名，map中key的值就是 字段的 as别名，
	* value值就是字段的值。param 为hql语句中参数值，如果hql中带有参数，param不为空 例如：
	* selec field1 as a ，field2 as b from Ttable where field3 =:asdf,param中就有一个元素，key=asdf,value=参数值，
	* 如果hql中没有参数，param置为NULL。
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
	 * 根据hql语句查询数据，返回的结果集list 为map元素集合。hql 中检索字段的字段一定要到有 as 别名，map中key的值就是 字段的 as别名，
	 * value值就是字段的值。param 为hql语句中参数值，如果hql中带有参数，param不为空 例如：
	 * selec field1 as a ，field2 as b from Ttable where field3 =:asdf,param中就有一个元素，key=asdf,value=参数值，
	 * 如果hql中没有参数，param置为NULL。
	 * 
	 * 当targetPage=-1时 表示不分页。	
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

		if (targetPage != -1) {// 等于-1时，不作分页
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
		if (name != null && name.length > 1 && list.get(0) instanceof Object[]) {// 对象数组
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
		} else {// 对象
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
	 * 以升序或者降序的规则，获得一个表的所有记录
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
	 * 升序或者降序的规则，获得一个表的分页记录
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
	 * 按HQL查询对象列表返回的结果集是Object数组的List.
	 * 
	 * @param hql
	 *            hql语句
	 * @param param
	 *            参数名值对，如果没有参数请置为null。
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
	 * 按HQL查询对象列表返回的结果集是Object数组的List.
	 * 
	 * @param hql
	 *            hql语句
	 * @param param
	 *            参数名值对，如果没有参数请置为null。
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
	 * 按HQL查询唯一对象.
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
	 * 根据Finder 查询数据
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
	 * 按HQL查询唯一对象.
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
	 * 按属性查找对象列表.
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
	 * 按属性查找唯一对象.
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
	 * 根据Finder 分页查询数据
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
	 * 根据Finder获得数据
	 * 
	 * @param finder
	 *@return
	 *@throws HibernateException
	 */
	public Pagination find(Finder finder) throws HibernateException {
		return find(finder, -1, -1);
	}

	/**
	 * 根据Conds he exclude条件获得 结果
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
			throw new HibernateException("所更新的对象没有映射或不是实体对象");
		}
		Object bean = updater.getBean();
		Session session = getSession();
		Object po = session.load(bean.getClass(), cm.getIdentifier(bean));
		updaterCopyToPersistentObject(updater, po);
		return po;
	}

	/**
	 * 根据查询函数与参数列表创建Query对象,后续可进行更多处理,辅助函数.
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
	 * 通过count查询获得本次查询所能获得的对象总数.
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
	 * 通过count查询获得本次查询所能获得的对象总数.
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
		// 处理排序和is null字段
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
		// 处理many to one查询
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
	 * 将更新对象拷贝至实体对象，并处理many-to-one的更新。
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
					// 如果更新的many to one的对象的id为空，则将many to one设置为null。
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
				logger.warn("更新对象时，拷贝属性异常", e);
			}
		}
	}

	/**
	 * 根据Criterion条件创建Criteria,后续可进行更多处理,辅助函数.
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
	 * 取得pojo中所有成员变量名
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
	 * 不为空的EXAMPLE属性选择方式
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
