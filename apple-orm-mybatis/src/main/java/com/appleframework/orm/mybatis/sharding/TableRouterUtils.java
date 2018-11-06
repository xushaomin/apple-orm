package com.appleframework.orm.mybatis.sharding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 分表路由工具类
 */
public class TableRouterUtils {

	private final int NODE_NUM = 100;
	private TreeMap<Long, Object> nodes;
	private List<Object> shards;

	private TableRouterUtils(List<Object> shards) {
		this.shards = shards;
		init();
	}

	private void init() { // 初始化一致性hash环
		nodes = new TreeMap<Long, Object>();
		for (int i = 0; i != shards.size(); ++i) {
			final Object shardInfo = shards.get(i);
			for (int n = 0; n < NODE_NUM; n++) {
				nodes.put(hash("SHARD-" + i + "-NODE-" + n), shardInfo);
			}
		}
	}

	public Object getShardInfo(String key) {
		SortedMap<Long, Object> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
		if (tail.size() == 0) {
			return nodes.get(nodes.firstKey());
		}
		return tail.get(tail.firstKey()); // 返回该虚拟节点对应的真实机器节点的信息
	}

	public Long hash(String key) {
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
		int seed = 0x1234ABCD;
		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		long m = 0xc6a4a7935bd1e995L;
		int r = 47;
		long h = seed ^ (buf.remaining() * m);
		long k;
		while (buf.remaining() >= 8) {
			k = buf.getLong();
			k *= m;
			k ^= k >>> r;
			k *= m;
			h ^= k;
			h *= m;
		}
		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			finish.put(buf).rewind();
			h ^= finish.getLong();
			h *= m;
		}
		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;
		buf.order(byteOrder);
		return h;
	}

	public static class TableRouterHolder {
		private volatile static TableRouterUtils instance;
		public static TableRouterUtils instance(List<Object> shards) {
			if (instance == null) {
				synchronized (TableRouterHolder.class) {
					if (instance == null) {
						instance = new TableRouterUtils(shards);
					}
				}
			}
			return instance;
		}
	}
}
