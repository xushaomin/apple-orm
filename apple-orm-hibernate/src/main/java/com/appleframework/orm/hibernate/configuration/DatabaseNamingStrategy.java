package com.appleframework.orm.hibernate.configuration;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;

/**
 * 数据库命名策略
 */

public class DatabaseNamingStrategy extends DefaultComponentSafeNamingStrategy {

	private static final long serialVersionUID = 4614294361681654588L;
	
	private String tablePrefix;// 数据库表名前缀
	private Boolean isAddUnderscores;// 是否以下划线形式命名
	private Integer maxLength;// 命名最大长度限制
	
	protected static String addUnderscores(String name) {
		if (name == null) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer(name.replace('.', '_'));
		for (int i = 1; i < stringBuffer.length() - 1; i++) {
			if (Character.isLowerCase(stringBuffer.charAt(i-1)) && Character.isUpperCase(stringBuffer.charAt(i)) && Character.isLowerCase(stringBuffer.charAt(i+1))) {
				stringBuffer.insert(i++, '_');
			}
		}
		return stringBuffer.toString().toLowerCase();
	}
	
	@Override
	public String classToTableName(String className) {
		if (isAddUnderscores) {
			return StringUtils.substring(tablePrefix + super.classToTableName(addUnderscores(className)), 0, maxLength);
		} else {
			return StringUtils.substring(tablePrefix + super.classToTableName(className), 0, maxLength);
		}
	}

	@Override
	public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
		if (isAddUnderscores) {
			return StringUtils.substring(tablePrefix + super.collectionTableName(addUnderscores(ownerEntity), addUnderscores(ownerEntityTable), addUnderscores(associatedEntity), addUnderscores(associatedEntityTable), addUnderscores(propertyName)), 0, maxLength);
		} else {
			return StringUtils.substring(tablePrefix + super.collectionTableName(ownerEntity, ownerEntityTable, associatedEntity, associatedEntityTable, propertyName), 0, maxLength);
		}
	}

	@Override
	public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
		if (isAddUnderscores) {
			return StringUtils.substring(tablePrefix + super.logicalCollectionTableName(addUnderscores(tableName), addUnderscores(ownerEntityTable), addUnderscores(associatedEntityTable), addUnderscores(propertyName)), 0, maxLength);
		} else {
			return StringUtils.substring(tablePrefix + super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName), 0, maxLength);
		}
	}
	
	@Override
	public String propertyToColumnName(String propertyName) {
		if (isAddUnderscores) {
			return StringUtils.substring(super.propertyToColumnName(addUnderscores(propertyName)), 0, maxLength);
		} else {
			return StringUtils.substring(super.propertyToColumnName(propertyName), 0, maxLength);
		}
	}

	@Override
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
		if (isAddUnderscores) {
			return StringUtils.substring(super.foreignKeyColumnName(addUnderscores(propertyName), addUnderscores(propertyEntityName), addUnderscores(propertyTableName), addUnderscores(referencedColumnName)), 0, maxLength);
		} else {
			return StringUtils.substring(super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName), 0, maxLength);
		}
	}

	@Override
	public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
		if (isAddUnderscores) {
			return StringUtils.substring(super.logicalCollectionColumnName(addUnderscores(columnName), addUnderscores(propertyName), addUnderscores(referencedColumn)), 0, maxLength);
		} else {
			return StringUtils.substring(super.logicalCollectionColumnName(columnName, propertyName, referencedColumn), 0, maxLength);
		}
	}

	@Override
	public String logicalColumnName(String columnName, String propertyName) {
		if (isAddUnderscores) {
			return StringUtils.substring(super.logicalColumnName(addUnderscores(columnName), addUnderscores(propertyName)), 0, maxLength);
		} else {
			return StringUtils.substring(super.logicalColumnName(columnName, propertyName), 0, maxLength);
		}
	}

	@Override
	public String joinKeyColumnName(String joinedColumn, String joinedTable) {
		if (isAddUnderscores) {
			return StringUtils.substring(super.joinKeyColumnName(addUnderscores(joinedColumn), addUnderscores(joinedTable)), 0, maxLength);
		} else {
			return StringUtils.substring(super.joinKeyColumnName(joinedColumn, joinedTable), 0, maxLength);
		}
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public Boolean getIsAddUnderscores() {
		return isAddUnderscores;
	}

	public void setIsAddUnderscores(Boolean isAddUnderscores) {
		this.isAddUnderscores = isAddUnderscores;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

}