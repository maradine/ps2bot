import net.spy.memcached.MemcachedClient;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.AddrUtil;

class MemcachedHelper {


	public static void main(String args[]) throws Exception{
		MemcachedClient c=new MemcachedClient(new BinaryConnectionFactory(),AddrUtil.getAddresses("fkpk-cache.ibfx1l.0001.usw2.cache.amazonaws.com:11211"));
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("beer", 3600, "beer");
		c.set("boar", 3600, "beer");
		c.set("bear", 3600, "beer");
		c.set("bong", 3600, "beer");
	}

}
