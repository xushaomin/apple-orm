package com.appleframework.orm.hibernate.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.jdbc.support.JdbcUtils;

import com.appleframework.orm.hibernate.model.unified.AbstractPersistentEntity;
import com.appleframework.orm.hibernate.model.unified.IBO;

/**
 * This is utility class, that some useful method for handling jdbc stuff.
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
public final class DataAccessUtility {
	
	private static Logger logger = Logger.getLogger(DataAccessUtility.class);

	/**
	 * @param <T>
	 *            element type, must extends {@link AbstractPersistentEntity}.
	 * @param list
	 *            target collection.
	 * @return the first(random) element of collection if available, else
	 *         returns <code>null</code>.
	 */
	public static <T extends AbstractPersistentEntity> T fetchFirstRowOrNull(final Collection<T> list) {
		final Iterator<T> iterator = list.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	/**
	 * check if all tables from given array/collection exist in database.
	 * 
	 * @param connection
	 *            actual jdbc connection
	 * @param tables
	 *            array of tables
	 * @return true if all tables from given collection exist.
	 * @throws java.sql.SQLException
	 *             if JDBC exception occurred
	 */
	public static boolean allTablesExist(Connection connection, String[] tables) throws SQLException {
		boolean tablesCreated = true;
		for (String table : tables) {
			ResultSet resultSet = null;
			try {
				resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, table, null);
				if (!resultSet.next()) {
					tablesCreated = false;
					logger.debug("table " + table + " does not exist");
				} else {
					logger.debug("table " + table + "  already exists");
				}
			} finally {
				JdbcUtils.closeResultSet(resultSet);
			}
		}
		logger.debug("tablesCreated(connection=>" + connection + ",tables=>" + tables + "):=" + tablesCreated + "]");
		return tablesCreated;
	}

	/**
	 * looking for initial revision of entity(for first database insert).
	 * 
	 * @param auditReader
	 *            envers reader implementation
	 * @param persistentClass
	 *            something that extends {@link AbstractPersistentEntity}
	 * @param uniqueIdentifier
	 *            primary key of entity
	 * @return revision number
	 */
	public static Number initialRevision(AuditReader auditReader, Class<? extends IBO> persistentClass, long uniqueIdentifier) {
		return (Number) auditReader.createQuery()
				.forRevisionsOfEntity(persistentClass, true, true)
				.add(AuditEntity.id().eq(uniqueIdentifier))
				.add(AuditEntity.revisionType().eq(RevisionType.ADD))
				.addProjection(AuditEntity.revisionNumber().min())
				.getSingleResult();
	}

	/**
	 * looking for latest modify revision of entity(for latest database
	 * update/delete).
	 * 
	 * @param auditReader
	 *            envers reader implementation
	 * @param persistentClass
	 *            something that extends {@link AbstractPersistentEntity}
	 * @param uniqueIdentifier
	 *            primary key of entity
	 * @return revision number
	 */
	public static Number latestModifyRevision(AuditReader auditReader, Class<? extends IBO> persistentClass, long uniqueIdentifier) {
		return (Number) auditReader
				.createQuery()
				.forRevisionsOfEntity(persistentClass, true, true)
				.add(AuditEntity.id().eq(uniqueIdentifier))
				.add(AuditEntity.revisionType()
				.in(new RevisionType[] { RevisionType.MOD, RevisionType.DEL }))
				.addProjection(AuditEntity.revisionNumber().max())
				.getSingleResult();
	}

	private DataAccessUtility() {
	}
}
