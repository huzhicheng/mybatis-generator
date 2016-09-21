
import org.mybatis.generator.api.ShellRunner;

/**
 * 
 */

/**
 * @author yangqx
 *
 */
public class DaoGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String mysql_config=DaoGenerator.class.getResource("/generatorMybatis-mysql.xml").getPath();
		ShellRunner.main(new String[]{"-configfile", mysql_config, "-overwrite"});
	}

}
