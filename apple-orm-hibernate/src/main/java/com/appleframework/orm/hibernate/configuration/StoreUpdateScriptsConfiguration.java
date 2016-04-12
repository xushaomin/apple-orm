package com.appleframework.orm.hibernate.configuration;

import java.io.Serializable;

import com.appleframework.orm.hibernate.common.annotation.ValueObject;


/**
 * 
 * @author Cruise.Xu
 * @date: 2011-9-8
 * 
 */
@ValueObject
public class StoreUpdateScriptsConfiguration implements Serializable {

	private static final long serialVersionUID = -403132813951950114L;
	private boolean generateSchemaUpdateScript = true, generateSchemaCreateScript, generateSchemaDropScript;

	/**
	 * schema update script was generated?
	 * 
	 * @return true if schema update script was generated during spring
	 *         application context initialization.
	 */
	public boolean isGenerateSchemaUpdateScript() {
		return generateSchemaUpdateScript;
	}

	/**
	 * schema create script was generated?
	 * 
	 * @return true if schema create script was generated during spring
	 *         application context initialization.
	 */
	public boolean isGenerateSchemaCreateScript() {
		return generateSchemaCreateScript;
	}

	/**
	 * schema delete script was generated?
	 * 
	 * @return true if schema delete script was generated during spring
	 *         application context initialization.
	 */
	public boolean isGenerateSchemaDropScript() {
		return generateSchemaDropScript;
	}

	/**
	 * enable/disable schema creation script generation logic.
	 * <p/>
	 * script will be stored into
	 * <code>./data/schema_update/System.currentTimeMillis.sql</code> file by
	 * default.
	 * <p/>
	 * NOTE : if this feature enabled please ensure directory
	 * ./data/schema_update/ is writable.
	 * 
	 * @param generateSchemaUpdateScript
	 *            true/false flag.
	 */
	public void setGenerateSchemaUpdateScript(boolean generateSchemaUpdateScript) {
		this.generateSchemaUpdateScript = generateSchemaUpdateScript;
	}

	/**
	 * enable/disable schema update script generation logic.
	 * <p/>
	 * script will be stored into
	 * <code>./data/schema_update/System.currentTimeMillis.sql</code> file by
	 * default.
	 * <p/>
	 * NOTE : if this feature enabled please ensure directory
	 * ./data/schema_update/ is writable.
	 * 
	 * @param generateSchemaCreateScript
	 *            true/false flag.
	 */
	public void setGenerateSchemaCreateScript(boolean generateSchemaCreateScript) {
		this.generateSchemaCreateScript = generateSchemaCreateScript;
	}

	/**
	 * enable/disable schema update script generation logic.
	 * <p/>
	 * script will be stored into
	 * <code>./data/schema_drop/System.currentTimeMillis.sql</code> file by
	 * default.
	 * <p/>
	 * NOTE : if this feature enabled please ensure directory
	 * ./data/schema_drop/ is writable.
	 * 
	 * @param generateSchemaDropScript
	 *            true/false flag.
	 */
	public void setGenerateSchemaDropScript(boolean generateSchemaDropScript) {
		this.generateSchemaDropScript = generateSchemaDropScript;
	}
}