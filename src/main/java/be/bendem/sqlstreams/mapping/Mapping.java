package be.bendem.sqlstreams.mapping;

import be.bendem.sqlstreams.util.SqlFunction;
import be.bendem.sqlstreams.util.Tuple2;

import java.sql.ResultSet;

public final class Mapping {

    private Mapping() {}

    public static <R> SqlFunction<ResultSet, R> to(Class<R> clazz) {
        return ClassMapping.get(clazz);
    }

    public static <R> SqlFunction<ResultSet, R> to(Class<R> clazz, String... names) {
        return ColumnNamesClassMapping.get(clazz, names);
    }

    public static <R> SqlFunction<ResultSet, R> to(Class<R> clazz, int... columns) {
        return ColumnClassMapping.get(clazz, columns);
    }

    public static <Left, Right> SqlFunction<ResultSet, Tuple2<Left, Right>> join(Class<Left> leftClass, Class<Right> rightClass) {
        return rs -> ClassMapping.combine(rs, ClassMapping.get(leftClass), ClassMapping.get(rightClass));
    }

}
