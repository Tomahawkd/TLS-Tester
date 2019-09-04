package io.tomahawkd.detect.database.mapper;

import io.tomahawkd.detect.TreeCode;
import io.tomahawkd.detect.database.model.DefaultRecord;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DefaultRecordMapper {

	String LEAKY_NAME = "leaky";
	String TAINTED_NAME = "tainted";
	String PARTIAL_NAME = "partial";

	@Select("select ip, port, country, ssl_enabled, `" +
			LEAKY_NAME + "`, `" + TAINTED_NAME + "`, `" + PARTIAL_NAME + "`, hash " +
			"from `default` where #{ip} = ip and #{port} = port limit 0,1;")
	@Results({
			@Result(property = "ip", column = "ip", javaType = String.class),
			@Result(property = "port", column = "port", javaType = int.class),
			@Result(property = "country", column = "country", javaType = String.class),
			@Result(property = "sslEnabled", column = "ssl_enabled", javaType = boolean.class),
			@Result(property = "leaky", column = LEAKY_NAME, javaType = TreeCode.class),
			@Result(property = "tainted", column = TAINTED_NAME, javaType = TreeCode.class),
			@Result(property = "partial", column = PARTIAL_NAME, javaType = TreeCode.class),
			@Result(property = "hash", column = "hash", javaType = String.class)
	})
	DefaultRecord selectWhere(String ip, int port);

	@Insert("insert into `default` (ip, port, country, ssl_enabled, `" +
			LEAKY_NAME + "`, `" + TAINTED_NAME + "`, `" + PARTIAL_NAME + "`, hash) " +
			"VALUES (#{record.ip}, #{record.port}, #{record.country}, #{record.sslEnabled}, " +
			"#{record.leaky,typeHandler=TreeCodeTypeHandler}, " +
			"#{record.tainted,typeHandler=TreeCodeTypeHandler}, " +
			"#{record.partial,typeHandler=TreeCodeTypeHandler}, #{record.hash})")
	int insertRecord(@Param("record") DefaultRecord record);
}
