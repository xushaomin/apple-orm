package com.appleframework.orm.mybatis.sharding;

/**
 * sql分析器
 */
public class SqlRewriter {

	/**
	 * 重写所给的语句，并且将旧的表名替换成新的表名。
	 *
	 * @param sql     - 重写的语句
	 * @param oldName - 旧数据表名
	 * @param newName - 新数据表名
	 * @return 重写后的语句
	 */
	public static String rewriteSqlTable(String sql, String oldName, String newName) {
		SqlTokenizer tokenizer = new SqlTokenizer(sql);
		tokenizer.setWantedComma(true); // 需要返回逗号
		String first = tokenizer.nextToken();
		if ("SELECT".equalsIgnoreCase(first)) {
			// 重写 SELECT / DELETE 语句
			return rewriteSelectTable(tokenizer, oldName, newName);
		} else if ("UPDATE".equalsIgnoreCase(first)) {
			// 重写 UPDATE 语句
			return rewriteUpdateTable(tokenizer, oldName, newName);
		} else if ("INSERT".equalsIgnoreCase(first) || "REPLACE".equalsIgnoreCase(first)) {
			// 重写 INSERT / REPLACE 语句
			return rewriteInsertTable(tokenizer, oldName, newName);
		} else if ("DELETE".equalsIgnoreCase(first)) {
			// 重写 DELETE 语句
			return rewriteDeleteTable(tokenizer, oldName, newName);
		} else {
			sql = sql.replaceAll(oldName, newName);
		}
		return sql; // 无法重写
	}

	/**
	 * 重写所给的 INSERT / REPLACE 语句，并且将旧的表名替换成新的表名。
	 *
	 * @param tokenizer - 解析的 INSERT 语句
	 * @param oldName   - 旧数据表名
	 * @param newName   - 新数据表名
	 * @return 重写后的语句
	 */
	private static String rewriteInsertTable(SqlTokenizer tokenizer, String oldName, String newName) {

		String sql = tokenizer.getSql();

		// 重写 INSERT 语句中的表名为: suffix 的新表名。
		StringBuilder builder = new StringBuilder();

		int index = 0;

		// 处理 INSERT [INTO] ... [(...)] VALUES (...)
		while (tokenizer.moreTokens()) {

			// 表名的起始位置
			int begin = tokenizer.getOffset();
			String token = tokenizer.nextToken();
			// 不处理： INTO
			if ("INTO".equalsIgnoreCase(token)) {
				continue;
			}
			// 只处理： INSERT [INTO] ... [(...)] VALUES
			if ((token.charAt(0) == '(') || "VALUES".equalsIgnoreCase(token)) {
				break;
			}
			// 使用大小写敏感的表名匹配
			if (token.equals(oldName)) {
				// 写入新的表名, 注意 INSERT 不需要别名
				builder.append(sql.substring(index, begin));
				builder.append(newName);
				index = tokenizer.getOffset();
			}
		}

		// 处理 INSERT [INTO] ... [(...)] SELECT ... FROM ... WHERE ...
		if (tokenizer.findToken("FROM")) {
			// 重写 SELETE 中的表名为: suffix 的新表名。
			while (tokenizer.moreTokens()) {
				// 表名的起始位置
				int begin = tokenizer.getOffset();
				String token = tokenizer.nextToken();
				// 只处理： FROM ... WHERE
				if (token.equalsIgnoreCase("WHERE")) {
					break;
				}
				// 使用大小写敏感的表名匹配
				if (token.equals(oldName)) {
					// 表名的结束位置。
					int end = tokenizer.getOffset();
					boolean usingAlias = false;
					// 检查是否申明了表的别名 TABLE [AS] ALIAS
					if (tokenizer.moreTokens()) {
						token = tokenizer.nextToken();
						if (!",".equals(token) && !"WHERE".equalsIgnoreCase(token) && !"JOIN".equalsIgnoreCase(token)
								&& !"LEFT".equalsIgnoreCase(token) && !"RIGHT".equalsIgnoreCase(token)
								&& !"INNER".equalsIgnoreCase(token)) {
							// 表名后紧跟 AS 或者其他名称。
							usingAlias = true;
						}
						do {
							// 忽略 JOIN ... ON 部分的内容
							if (",".equals(token) || "WHERE".equalsIgnoreCase(token)) {
								break;
							}
							token = tokenizer.nextToken();
						} while (tokenizer.moreTokens());
					}
					// 如果已经声明别名，则不需要设置别名
					builder.append(sql.substring(index, begin));
					builder.append(newName);
					if (!usingAlias) {
						// 如果没有声明别名，则将原始表名作为别名
						builder.append(' ');
						builder.append(oldName);
					}
					index = end;
				}
			}
		}
		if (index < sql.length()) {
			// 写入最后的部分: SET ...WHERE ...
			builder.append(sql.substring(index, sql.length()));
		}
		if (index == 0) {
			// 处理最有名称没有更改的问题
			return builder.toString().replace(oldName, newName);
		}
		return builder.toString();
	}

	/**
	 * 重写所给的 UPDATE 语句，并且将旧的表名替换成新的表名。
	 *
	 * @param tokenizer - 解析的 UPDATE 语句
	 * @param oldName   - 旧数据表名
	 * @param newName   - 新数据表名
	 * @return 重写后的语句
	 */
	private static String rewriteUpdateTable(SqlTokenizer tokenizer, String oldName, String newName) {
		String sql = tokenizer.getSql();

		// 重写 UPDATE 语句中的表名为: suffix 的新表名。
		StringBuilder builder = new StringBuilder();
		int index = 0;
		// 处理 UPDATE ... SET
		while (tokenizer.moreTokens()) {
			// 表名的起始位置
			int begin = tokenizer.getOffset();
			String token = tokenizer.nextToken();
			// 只处理： UPDATE ... SET
			if ("SET".equalsIgnoreCase(token)) {
				break;
			}
			// 使用大小写敏感的表名匹配
			if (token.equals(oldName)) {
				// 表名的结束位置。
				int end = tokenizer.getOffset();
				boolean usingAlias = false;
				// 检查是否申明了表的别名 TABLE [AS] ALIAS
				if (tokenizer.moreTokens()) {
					token = tokenizer.nextToken();
					if (!",".equals(token) && !"SET".equalsIgnoreCase(token) && !"JOIN".equalsIgnoreCase(token)
							&& !"LEFT".equalsIgnoreCase(token) && !"RIGHT".equalsIgnoreCase(token)
							&& !"INNER".equalsIgnoreCase(token)) {
						// 表名后紧跟 AS 或者其他名称。
						usingAlias = true;
					}
					do {
						// 忽略 JOIN ... ON 部分的内容
						if (",".equals(token) || "SET".equalsIgnoreCase(token)) {
							break;
						}
						token = tokenizer.nextToken();
					} while (tokenizer.moreTokens());
				}

				// 如果已经声明别名，则不需要设置别名
				builder.append(sql.substring(index, begin));
				builder.append(newName);
				if (!usingAlias) {
					// 如果没有声明别名，则将原始表名作为别名
					builder.append(' ');
					builder.append(oldName);
				}
				index = end;
			}
		}
		if (index < sql.length()) {
			// 写入最后的部分: SET ...WHERE ...
			builder.append(sql.substring(index, sql.length()));
		}
		return builder.toString();
	}

	/**
	 * 重写所给的 SELECT 语句，并且将旧的表名替换成新的表名。
	 *
	 * @param tokenizer - 解析的 SELECT 语句
	 * @param oldName   - 旧数据表名
	 * @param newName   - 新数据表名
	 * @return 重写后的语句
	 */
	private static String rewriteSelectTable(SqlTokenizer tokenizer, String oldName, String newName) {
		String sql = tokenizer.getSql();
		// 忽略 SELECT ... FROM
		if (tokenizer.findToken("FROM")) {
			// 重写 SELETE 中的表名为: suffix 的新表名。
			StringBuilder builder = new StringBuilder();
			int index = 0;
			while (tokenizer.moreTokens()) {
				// 表名的起始位置
				int begin = tokenizer.getOffset();
				String token = tokenizer.nextToken();
				// 只处理： FROM ... WHERE
				if (token.equalsIgnoreCase("WHERE")) {
					break;
				}
				// 使用大小写敏感的表名匹配
				if (token.equals(oldName)) {
					// 表名的结束位置。
					int end = tokenizer.getOffset();
					boolean usingAlias = false;
					// 检查是否申明了表的别名 TABLE [AS] ALIAS
					if (tokenizer.moreTokens()) {
						token = tokenizer.nextToken();
						if (!",".equals(token) && !"WHERE".equalsIgnoreCase(token) && !"JOIN".equalsIgnoreCase(token)
								&& !"LEFT".equalsIgnoreCase(token) && !"RIGHT".equalsIgnoreCase(token)
								&& !"INNER".equalsIgnoreCase(token)) {

							// 表名后紧跟 AS 或者其他名称。
							usingAlias = true;
						}
						do {
							// 忽略 JOIN ... ON 部分的内容
							if (",".equals(token) || "WHERE".equalsIgnoreCase(token)) {
								break;
							}
							token = tokenizer.nextToken();
						} while (tokenizer.moreTokens());
					}
					// 如果已经声明别名，则不需要设置别名
					builder.append(sql.substring(index, begin));
					builder.append(newName);
					if (!usingAlias) {
						// 如果没有声明别名，则将原始表名作为别名
						builder.append(' ');
						builder.append(oldName);
					}
					index = end;
				}
			}
			if (index < sql.length()) {
				// 写入最后的部分: WHERE ...
				builder.append(sql.substring(index, sql.length()));
			}
			return builder.toString();
		}
		return sql;
	}

	/**
	 * 重写所给的 DELETE 语句，并且将旧的表名替换成新的表名。
	 *
	 * @param tokenizer - 解析的 DELETE 语句
	 * @param oldName   - 旧数据表名
	 * @param newName   - 新数据表名
	 * @return 重写后的语句
	 */
	private static String rewriteDeleteTable(SqlTokenizer tokenizer, String oldName, String newName) {
		String sql = tokenizer.getSql();
		// 忽略 DELETE FROM
		if (tokenizer.findToken("FROM")) {
			// 重写 DELETE 中的表名为: suffix 的新表名。
			StringBuilder builder = new StringBuilder();
			int index = 0;
			while (tokenizer.moreTokens()) {
				// 表名的起始位置
				int begin = tokenizer.getOffset();
				String token = tokenizer.nextToken();
				// 只处理： FROM ... WHERE
				if (token.equalsIgnoreCase("WHERE")) {
					break;
				}
				// 使用大小写敏感的表名匹配
				if (token.equals(oldName)) {
					// 写入新的表名, 注意 DELETE 不需要别名
					builder.append(sql.substring(index, begin));
					builder.append(newName);
					index = tokenizer.getOffset();
				}
			}
			if (index < sql.length()) {
				// 写入最后的部分: WHERE ...
				builder.append(sql.substring(index, sql.length()));
			}
			return builder.toString();
		}

		return sql;
	}
}
