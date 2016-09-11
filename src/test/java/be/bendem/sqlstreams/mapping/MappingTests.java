package be.bendem.sqlstreams.mapping;

import be.bendem.sqlstreams.MappingConstructor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MappingTests {

    static private class User {
        public User(int id, String name, String password) {
            this(id, name, password, false);
        }

        @MappingConstructor
        public User(int id, String name, String password, boolean activated) {
        }
    }

    static private class UserNamePass {
        String name;
        String password;

        public UserNamePass(String name, String password) {
            this.name = name;
            this.password = password.trim();
        }
    }

    static private class DatesAndTimes {
        private final LocalTime time;
        private final LocalDate date;
        private final LocalDateTime datetime;

        public DatesAndTimes(LocalTime time, LocalDate date, LocalDateTime datetime) {
            this.time = time;
            this.date = date;
            this.datetime = datetime;
        }
    }

    static private class NoConstructor {}

    static private class MultipleMarkedConstructor {
        @MappingConstructor
        public MultipleMarkedConstructor() {}

        @MappingConstructor
        public MultipleMarkedConstructor(Void ignore) {}
    }

    static private class MultipleNonMarkedConstructor {
        public MultipleNonMarkedConstructor(boolean bool) {}

        public MultipleNonMarkedConstructor(int i) {}
    }

    static private class InvalidConstructor {
        public InvalidConstructor(User user) {}
    }

    @Test
    public void testMapToSingleConstructorClass() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(1)).thenReturn("value1");
        Mockito.when(rs.getString(2)).thenReturn("value2\t");

        UserNamePass result = Mapping.to(UserNamePass.class).apply(rs);

        Assert.assertEquals("value1", result.name);
        Assert.assertEquals("value2", result.password);
    }

    @Test
    public void testColumnNamesMapping() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString("name")).thenReturn("value1");
        Mockito.when(rs.getString("password")).thenReturn("value2\t");

        UserNamePass result = Mapping.to(UserNamePass.class, "name", "password").apply(rs);

        Assert.assertEquals("value1", result.name);
        Assert.assertEquals("value2", result.password);
    }

    @Test
    public void testColumnIndexesMapping() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(2)).thenReturn("value1");
        Mockito.when(rs.getString(3)).thenReturn("value2\t");

        UserNamePass result = Mapping.to(UserNamePass.class, 2, 3).apply(rs);

        Assert.assertEquals("value1", result.name);
        Assert.assertEquals("value2", result.password);
    }

    @Test
    public void testJava8TimeMapping() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getTime(1)).thenReturn(new Time(13, 14, 15));
        Mockito.when(rs.getDate(2)).thenReturn(new Date(2016 - 1900, 11 - 1, 13));
        Mockito.when(rs.getTimestamp(3)).thenReturn(new Timestamp(2016 - 1900, 11 - 1, 13, 13, 14, 15, 0));

        DatesAndTimes result = Mapping.to(DatesAndTimes.class).apply(rs);

        LocalTime date = LocalTime.of(13, 14, 15);
        LocalDate time = LocalDate.of(2016, 11, 13);
        LocalDateTime dateTime = LocalDateTime.of(time, date);
        Assert.assertEquals(date, result.time);
        Assert.assertEquals(time, result.date);
        Assert.assertEquals(dateTime, result.datetime);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoConstructorMatchingColumnMapping() {
        Mapping.to(User.class, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoConstructorMapping() {
        Mapping.to(NoConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleMarkedConstructors() {
        Mapping.to(MultipleMarkedConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleNonMarkedConstructorsWithColumnsSpecified() {
        Mapping.to(MultipleNonMarkedConstructor.class, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleNonMarkedConstructors() {
        Mapping.to(MultipleNonMarkedConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructorMapping() {
        Mapping.to(InvalidConstructor.class);
    }

}
