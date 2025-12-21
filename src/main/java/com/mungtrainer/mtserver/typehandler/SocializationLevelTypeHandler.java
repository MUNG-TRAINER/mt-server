package com.mungtrainer.mtserver.typehandler;

import com.mungtrainer.mtserver.dog.entity.SocializationLevel;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SocializationLevel Enum과 DB VARCHAR 간 변환을 위한 TypeHandler
 * MyBatis가 Enum을 DB의 VARCHAR 값과 자동으로 매핑하도록 지원
 */
@MappedTypes(SocializationLevel.class)
public class SocializationLevelTypeHandler extends BaseTypeHandler<SocializationLevel> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SocializationLevel parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public SocializationLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : SocializationLevel.valueOf(value.toUpperCase());
    }

    @Override
    public SocializationLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : SocializationLevel.valueOf(value.toUpperCase());
    }

    @Override
    public SocializationLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : SocializationLevel.valueOf(value.toUpperCase());
    }
}

